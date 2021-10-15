echo 'Creating key pair'
aws ec2 create-key-pair --key-name emr --region us-east-1 > emr.pem
