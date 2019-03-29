# How to run the script

Navigate to the infrastructure/aws/cloudformation folder in the terminal

Enter ./csye6225-aws-cf-create-stack.sh stack_name
Navigate to the AWS Console and verify that a VPC, Internet Gateway and a Route Table with the IGW attached to it
To terminate the stack enter ./csye6225-aws-cf-terminate-stack.sh stack_name

Run cicd stack and then the serverless stack followed by the  application stack
