# csye6225-fall2018
# assignment09-10

# Team Member Information:
- Akilan Rajendiran         rajendiran.a@husky.neu.edu
- Menita Koonani            koonani.m@husky.neu.edu
- Neha Pednekar             pednekar.n@husky.neu.edu
- Raghavi Kirouchenaradjou  kirouchenaradjou.r@husky.neu.edu 


# Prerequisites for build and deployment of the web app

- Should have installed IntelliJ IDEA and MySQL server to run the app locally
- JUnit must be installed for Unit testing of the code
- Should have Advanced Rest client/POSTMAN installed as a Chrome extension

# Instructions to build and deploy the web app

- App must be imported in the IntelliJ and MySQL server must be configured to run the app.
- The application.properties should be set on your local to match your MySQL password for the root user.
- To run the Spring Boot application, click on the green run button on the top panel.
- There will be two profiles, the dev profile to run the application on your local machine and aws profile to run the application in   AWS using S3 bucket utility
- Set the profile in the 'Edit Configurations' by mentioning the profile name in the active profile textbox.

# How to run the APi
# Run it via either Postman or AdvancedRestClient 
Following are the 2 API's been supported :

http://localhost:8080/user/register
method : post 
{"userName" : "user1@gmail.com",
"password" : "user"}

Response :
{
"message": "Registration Successful"
}


http://localhost:8080/user/register
method : post 
{"userName" : "user1@gmail.com",
"password" : "user"}

Response :
{
"message": "User already exists!"
}


#Transaction API

http://localhost:8080/transaction
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method : post
status code: 200 Created
payload: {
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "description": "coffee",
  "merchant": "starbucks",
  "amount": 2.69,
  "date": "09/25/2018",
  "category": "food",
  "attachments": {
    "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
    "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
  }
}

Response : 
{
    "message": "Transaction  Successful"
}

http://localhost:8080/transaction
authorization : No Auth
method : post
status code: 401(Unauthorized)
payload: {
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "description": "coffee",
  "merchant": "starbucks",
  "amount": 2.69,
  "date": "09/25/2018",
  "category": "food",
  "attachments": {
    "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
    "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
  }
}

Response : 
{
    "message": "You are not logged in - Provide Username and Password!"
}

http://localhost:8080/transaction
authorization : Basic Auth(valid user)
method: post
payload: Incorrect Payload
statuc code: 400(Bad Request)

Response:
{
    "timestamp": "2018-10-04T20:07:28.968+0000",
    "status": 400,
    "error": "Bad Request",
    "message": "Required request body is missing: public org.springframework.http.ResponseEntity com.csye6255.web.application.fall2018.controller.TransactionController.createTransactions(javax.servlet.http.HttpServletRequest,com.csye6255.web.application.fall2018.pojo.Transaction)",
    "path": "/transaction"
}

http://localhost:8080/transaction
authorization : No Auth
method : get
status code: 401(Unauthorized)

Response : 
{
    "message": "You are not logged in!"
}

http://localhost:8080/transaction
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method : get
status code: 200 OK

Response : 
[
    {
        "id": "a943b525-6d7e-4df0-85f9-169cb09a5795",
        "description": "coffee",
        "merchant": "2.69",
        "amount": "09/25/2018",
        "date": "starbucks",
        "category": "food",
        "attachments": {
            "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
            "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
        }
    },
    {
        "id": "c411c9c5-7a2e-44df-9871-bb473bb7bcd0",
        "description": "coffee",
        "merchant": "2.69",
        "amount": "09/25/2018",
        "date": "starbucks",
        "category": "food",
        "attachments": {
            "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
            "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
        }
    }
]

http://localhost:8080/transaction/{transactionid}
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZccdsdsfXI= (invalid user)
method : put
status code: 401(Unauthorized)

Response : 
{
    "message": "User not found! - Try Logging in again"
}

http://localhost:8080/transaction/{transactionid} {where transactionid = "d290f1ee-6c54-4b01-90e6-d701748f0851"}
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method : put
status code: 201 Created
payload: {
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "description": "tea",
  "merchant": "kungfu",
  "amount": 2.69,
  "date": "09/25/2018",
  "category": "food"
}

Response (updated payload): 
{
    "id": "a943b525-6d7e-4df0-85f9-169cb09a5795",
    "description": "tea",
    "merchant": "2.69",
    "amount": "09/25/2018",
    "date": "kungfu",
    "category": "food",
    "attachments": {
        "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
        "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
    }
}

http://localhost:8080/transaction/{transactionid} {where trsnactionID = Invalid}
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method : put
status code: 400 bad request
payload: {
    "id": "a943b525-6d7e-4df0-85f9-169cb09a5795",
    "description": "tea",
    "merchant": "2.69",
    "amount": "09/25/2018",
    "date": "kungfu",
    "category": "food",
    "attachments": {
        "id": "ff87c898-efc4-48ae-83db-fd36aff86c18",
        "url": "https://bucket.me.csye6225.com.s3.amazonaws.com/1539450197747.jpeg"
    }
}

Response:
{
    "timestamp": "2018-10-04T20:07:28.968+0000",
    "status": 400,
    "error": "Bad Request",
    "message": "Required request body is missing: public org.springframework.http.ResponseEntity com.csye6255.web.application.fall2018.controller.TransactionController.createTransactions(javax.servlet.http.HttpServletRequest,com.csye6255.web.application.fall2018.pojo.Transaction)",
    "path": "/transaction"
}

http://localhost:8080/transaction/{transactionid} 
authorization : No Auth
method: put
payload: correct Payload
status code: 401 Unauthorized

http://localhost:8080/transaction/{transactionid} (where transactionid is valid)
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: delete
status code: 204 No Content

http://localhost:8080/transaction/{transactionid} (where transactionid is invalid)
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: delete
status code: 400 Bad Request

http://localhost:8080/transaction/{transactionid} (where transactionid is invalid)
authorization : No Auth
method: delete
status code: 401 Unauthorized

http://localhost:8080/transaction/{transactionid}/attachments
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: POST
status code: 200 OK
payload:
Multipart form-data with key = uploadReceipt and key = .png/.jpeg/.jpg image file to upload

response:
{
    "message": "File attached successfully"
}

http://localhost:8080/transaction/{transactionid}/attachments
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: GET
status code: 200 OK
response:
[
    {
        "id": "3839073c-ee96-4041-8ab5-3eef392a5218",
        "url": "C:\\Users\\Neha Pednekar\\Downloads\\csye6225-fall2018-assignment4\\csye6225-fall2018-assignment4\\webapp\\images\\1539443528965.jpg"
    }
]

http://localhost:8080/transaction/{transactionid}/attachments/{attachmentid}
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: PUT
status code: 200 OK
payload: Multipart form-data with key = uploadReceipt and key = .png/.jpeg/.jpg image file to upload
response:
{
    "message": "File updated successfully"
}

http://localhost:8080/transaction/{transactionid}/attachments/{attachmentid}
authorization : Basic dXNlcjFAZ21haWwuY29tOnVzZXI= (valid user)
method: DELETE
status code: 200 OK
response:
{
    "message": "File deleted successfully"
}

# AWS Cloud formation Instructions

# Setup requirements
csye6225-aws-cf-create-stack.sh for cloudformation to create a VPC stack
csye6225-aws-cf-terminate-stack.sh for cloudformation to terminate a VPC stack and clear resources
csye6225-cf-networking.json for the networking setup template
csye6225-aws-cf-create-application-stack.sh for cloudformation to create a Application stack
csye6225-aws-cf-terminate-application-stack.sh for cloudformation to terminate a Apllication stack and clear resources
csye6225-cf-application.json for the application setup template
A shell terminal with AWS CLI configured

Check if files have executing and read-write access, if not available please set the same

Navigate to the folder containing the files using the terminal

For creating stack run the following command with STACK_NAME(any name) as parameter ./csye6225-aws-cf-create-stack.sh STACK_NAME ./csye6225-aws-cf-create-application-stack.sh STACK_NAME 
or
sh csye6225-aws-cf-create-stack.sh STACK_NAME
sh csye6225-aws-cf-create-application-stack.sh STACK_NAME

For terminating stack run the following command with STACK_NAME(any name) as parameter ./csye6225-aws-cf-terminate-stack.sh STACK_NAME ./csye6225-aws-cf-terminate-application-stack.sh STACK_NAME 
or 
sh csye6225-aws-cf-terminate-stack.sh STACK_NAME
sh csye6225-aws-cf-terminate-application-stack.sh STACK_NAME

# Instructions to run Unit Test, Integration and Load tests

- To perform the Unit Testing of the modules and controller, run the JUnit tests.
- Need to validate the input via a payload along with username and password using the Advanced Rest Client/POSTMAN app.
- Need to validate the GET and POST requests with relevant JSON payloads

# Link to TravisCI build

  https://travis-ci.com/AkilanRajendiran/csye6225-fall2018

