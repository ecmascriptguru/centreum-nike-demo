# Nike Assignment

## Prepare AWS Accounts
This deliverable assumes that you use 3 aws accounts - a shared service account (for deployment purpose) and 2 accounts (one is for dev and another is production).

To make it easy, let's prepare a note for the account names. Please write down the 3 account IDs (12 digits) in a file
```
SharedAccount: <tools_account_id>
DevAccount: <dev_account_id>
ProdAccount: <prod_account_id>
```

If you wrote the 3 account IDs, then you are ready to start.

### Set up Development and Production account
- Log in AWS management console with your dev account credentials and open new cloudformation stack page. (You can click [Here](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/create/template))
- Choose `Upload a template file` link to choose [this file](cicd/target/cf-cross-account-role.yml)
- Click `Next` and give your prefered name in `Stack Name` and copy your `<tools_account_id>` and paste it into the `ToolsAccountId` field.
- Please keep clicking `Next` button and please be sure that you check in `I acknowledge that AWS CloudFormation might create IAM resources with custom names.` before clicking `Create stack` button.
- Just wait for the stack is completely created. And then you should keep a note for the cross account role. For your information, you just created 2 roles `cross-account-role-centreum-demo` and execution role is `execution-role-centreum-demo`, which will be used in tools account.
- Repeat the above steps for production account. Once it's done, nothing else for you to do in the dev or production account any more.

### Set up Tools account.
The tools account is the account where you will store the source code in CodeCommit and CI/CD(CodePipeline). I already created a cloudformation template to create all the required resources such as CodeCommit, CodeBuild, CodePipeline, and so on. Please follow the instructions below.
- Log in the tools account and go to cloud formations section.
- Please create a stack (like you did in the dev or pdouction) with a different cloudformation file - `cicd/source/pipeline.yml`. Here you just need to adjust the `DevAccountID` and `ProdAccountID` with the corresponding IDS (`<dev_account_id>` amd `<prod_account_id>`) that you noted down.
- Once the stack is created completely, you should be able to find a code repository in [CodeCommit](https://console.aws.amazon.com/codesuite/codecommit/repositories?region=us-east-1) page. The name is `centreum-demo`.
- If you configured the credentials for this the tools account on your local, you just need to clone by executing this command - `ssh://git-codecommit.us-east-1.amazonaws.com/v1/repos/centreum-demo`
- In the cloned directory, place the source code and push to `master` branch.
- Please go to `CodePipeline` section in AWS Management Console, then you will see a new pipeline created named `centreum-demo-master`. The pipeline will deploy everyting to you dev account first.


### How to test
I plaved a sample csv file in the working tree - `csv/contacts.csv`. You just need to copy to the s3 bucket by using the following command. Suppose that you want to test in dev account. You need to configure the credentials so as to execute the following command
```
export ENV_NAME=dev
$ aws s3 cp csv/contacts.csv s3://nike-dev.csv.import.centreum.com/uploads/*created_date=2021-10-16/contacts.csv
```

Once the file is uploaded, a corresponding lambda function is triggered and it provisions a new EMR job with proper workflows. For your information, it takes over 30 minutes to be done.

Give it about 40 minutes please. Once it's done you should be able to see the contacts by using the REST API. The endpoint can be grabbed from API Gateway of AWS management console.
