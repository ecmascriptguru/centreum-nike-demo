import boto3
import logging
import os
from datetime import datetime
from pathlib import Path

emr = boto3.client('emr')
s3 = boto3.resource('s3')
dynamodb = boto3.resource('dynamodb')
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)


def start_emr_job(event, context):
    try:
        cluster_id = emr.run_job_flow(
            Name='test_emr_job',
            LogUri="s3://{}".format(os.environ['EMR_LOGS_BUCKET']),
            ReleaseLabel='emr-5.33.1',
            Applications=[
                {
                    'Name': 'Hive'
                },
                {
                    'Name': 'Hue'
                },
                {
                    'Name': 'Mahout'
                },
                {
                    'Name': 'Pig'
                },
                {
                    'Name': 'Tez'
                },
            ],
            Instances={
                'InstanceGroups': [
                    {
                        'Name': "Master nodes",
                        'Market': 'ON_DEMAND',
                        'InstanceRole': 'MASTER',
                        'InstanceType': 'm1.medium',
                        'InstanceCount': 1,
                    },
                    {
                        'Name': "Slave nodes",
                        'Market': 'ON_DEMAND',
                        'InstanceRole': 'CORE',
                        'InstanceType': 'm1.medium',
                        'InstanceCount': 1,
                    }
                ],
                'KeepJobFlowAliveWhenNoSteps': False,
                'TerminationProtected': False,
                'Ec2SubnetId': os.environ['SUBNET_ID'],
            },
            Configurations=[
                {
                    'Classification': 'hive-site',
                    'Properties': {
                        'hive.execution.engine': 'mr'
                    }
                },
            ],
            Steps=[
                {
                    'Name': 'creating dynamodb table',
                    'ActionOnFailure': 'CONTINUE',
                    'HadoopJarStep': {
                        'Jar': 'command-runner.jar',
                        'Args': [
                            'hive-script',
                            '--run-hive-script',
                            '--args',
                            '-f',
                            f's3://{os.environ["CSV_IMPORT_BUCKET"]}/scripts/step1.q',
                            '-d',
                            f'DYNAMODBTABLE={os.environ["CONTACTS_TABLE"]}'
                        ]
                    }
                },
                {
                    'Name': 'creating csv table',
                    'ActionOnFailure': 'CONTINUE',
                    'HadoopJarStep': {
                        'Jar': 'command-runner.jar',
                        'Args': [
                            'hive-script',
                            '--run-hive-script',
                            '--args',
                            '-f',
                            f's3://{os.environ["CSV_IMPORT_BUCKET"]}/scripts/step2.q',
                            '-d',
                            f'INPUT=s3://{os.environ["CSV_IMPORT_BUCKET"]}',
                            '-d',
                            f'TODAY={datetime.today().strftime("%Y-%m-%d")}'
                        ]
                    }
                },
                {
                    'Name': 'adding partition',
                    'ActionOnFailure': 'CONTINUE',
                    'HadoopJarStep': {
                        'Jar': 'command-runner.jar',
                        'Args': [
                            'hive-script',
                            '--run-hive-script',
                            '--args',
                            '-f',
                            f's3://{os.environ["CSV_IMPORT_BUCKET"]}/scripts/step3.q',
                            '-d',
                            f'INPUT=s3://{os.environ["CSV_IMPORT_BUCKET"]}',
                            '-d',
                            f'TODAY={datetime.today().strftime("%Y-%m-%d")}'
                        ]
                    }
                },
                {
                    'Name': 'import data to dynamodb',
                    'ActionOnFailure': 'TERMINATE_CLUSTER',
                    'HadoopJarStep': {
                        'Jar': 'command-runner.jar',
                        'Args': [
                            'hive-script',
                            '--run-hive-script',
                            '--args',
                            '-f',
                            f's3://{os.environ["CSV_IMPORT_BUCKET"]}/scripts/step4.q',
                            '-d',
                            f'TODAY={datetime.today().strftime("%Y-%m-%d")}'
                        ]
                    }
                }
            ],
            VisibleToAllUsers=True,
            JobFlowRole=os.environ.get('INSTANCE_PROFILE'),
            ServiceRole=os.environ.get('SERVICE_ROLE'),
        )
        logger.info('cluster {} created with the step...'.format(
            cluster_id['JobFlowId']))

    except Exception as e:
        logger.error(e)
        raise
