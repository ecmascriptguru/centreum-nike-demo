# Nike Assignment

## Architecture
This is the architecture diagram.
![](docs/architecture.png?raw=true)

## Prepare AWS Accounts
This deliverable assumes that you use 3 aws accounts - a Shared Service Deplpyment Account (for deployment purposes) and a Shared Service Developmenet and Shared Service Production Accounts (in which Solution Infrastructre is provisioned).

To make it easy, let's prepare a note for the account names. Please write down the 3 account IDs (12 digits) in a file
```
Shared Service Deployment Account: <tools_account_id>
Shared Service Development Account: <dev_account_id>
Shared Service Production Account: <prod_account_id>
```

If you have noted the 3 account IDs, then you are ready to start.

### Create IAM Roles in the Shared Service Development and Production account
- Log into Shared Service Development Acount's Management Console open new CloudFormation stack page. (You can click [Here](https://console.aws.amazon.com/CloudFormation/home?region=us-east-1#/stacks/create/template))
- Choose `Upload a template file` link to choose [cicd/cf-templates/target/cf-cross-account-role.yml](cicd/cf-templates/target/cf-cross-account-role.yml)
- Click `Next` and give your prefered name in `Stack Name` and copy your `<tools_account_id>` and paste it into the `ToolsAccountId` field.
- Please keep clicking `Next` button and please be sure that you tick `I acknowledge that AWS CloudFormation might create IAM resources with custom names.` before clicking `Create stack` button.
- Wait until the stack is created fully. For your information, you just created 2 roles 
    - `cross-account-role-centreum-demo` - which grants access to Shared Service Deployment Account to assume a role and other permissons in the Dev Account. 
    - `execution-role-centreum-demo` - which grants access to CloudFormation to create necessary resources
- Repeat the above steps for the Shared Service Production Account. 
- Once it's done, there is nothing else for you to do in the dev or production account anymore.

### Set up Shared Service Deployment Account
The Shared Service Deployment account is the account where you will store the source code in CodeCommit and execute the CI/CD Pipeline. I already created a CloudFormation template to create all the required resources such as CodeCommit, CodeBuild, CodePipeline, and so on. 
Please follow the instructions below.
- Log into Shared Service Deployment Acount's Management Console open new CloudFormation stack page. (You can click [Here](https://console.aws.amazon.com/CloudFormation/home?region=us-east-1#/stacks/create/template))
- Please create a stack with the file [cicd/cf-templates/target/pipeline.yml](cicd/cf-templates/source/pipeline.yml). Here you just need to adjust the `DevAccountID` and `ProdAccountID` with the corresponding IDS (`<dev_account_id>` amd `<prod_account_id>`) that you noted down.
- Once the stack is created fully, you should be able to find a code repository in [CodeCommit](https://console.aws.amazon.com/codesuite/codecommit/repositories?region=us-east-1) page. The name is `centreum-demo`.
- If you have configured the credentials for the Deployment account on your local machine, you just need to clone by executing this command - `ssh://git-codecommit.us-east-1.amazonaws.com/v1/repos/centreum-demo`
- In the cloned directory, place the source code and push to `master` branch.
- Please go to `CodePipeline` section in AWS Management Console, then you will see a new pipeline created named `centreum-demo-master`. The pipeline will deploy everyting to you Development account first. You can then test the functionality in the Dev Account, and when you are satisfied, you can manually approve the deployment to Production Account.


### How to test
I have placed a sample CSV file in the working tree - `csv/contacts.csv`. You just need to copy to the s3 bucket by using the following command. Suppose that you want to test in dev account. You need to configure the credentials so as to execute the following command
```
export ENV_NAME=dev
$ aws s3 cp csv/contacts.csv s3://<s3-bucket-name>/uploads/created_date=2021-10-16/contacts.csv
```

Once the file is uploaded, a corresponding lambda function is triggered and it provisions a new EMR job with proper workflows. For your information, it takes over 30 minutes for the transient EMR cluster to get provisioned, process the file and terminate automatically.

Once the EMR process completes, you should be able to see the contacts in the DynamoDB dataset. The DynamoDB dataset can be queried using the REST API. 

### Rest API
The API endpoint can be grabbed from API Gateway of AWS management console. The following APIs are supported -

#### List Contacts

    Request Method: GET
    URL: https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/contacts/
    Description: This functionality helps list all the contacts from the DynamoDB table.

#### Create Contact
    Request Method: POST
    URL: https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/contacts/
    Description: This functionality helps create a contact in the DynamoDB table.


Example: The following information can be POSTed to create a contact

```
{
    "fullName": "John Doe",
    "email": "johndoe@example.com",
    "gender": "Male",
    "address": "123 ABC Place"
}
```

The response will show the ID that will be assigned to the contact automatically. 


#### Get Contact with ID
    Request Method: GET
    URL: https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/contacts/{id}
    Description: This functionality helps get details of a particular contact from the DynamoDB table.

Example – To get details of ID = 228, click on the following URL –

    https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/contacts/228


#### Delete Contact with ID
    Request Method: DELETE
    URL: https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/contacts/{id}
    Description: This functionality helps delete a contact from the DynamoDB table.
