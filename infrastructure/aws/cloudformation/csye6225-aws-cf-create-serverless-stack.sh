STACK_NAME=$1


DOMAINNAME=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text)
DNS=${DOMAINNAME::-1}
echo $DNS

LambdaS3Bucket="lambda.${DNS}"
echo $LambdaS3Bucket

FromEmailAddress="noreply@${DNS}"
echo $FromEmailAddress


export lambdaRoleArn=$(aws iam list-roles --query 'Roles[*].[RoleName, Arn]' --output text | grep Lambda |awk '{print $2}')
echo "lambdaRoleArn : ${lambdaRoleArn}"

aws cloudformation create-stack --stack-name $STACK_NAME --capabilities "CAPABILITY_NAMED_IAM" --template-body file://csye6225-cf-serverless.json --parameters ParameterKey=LambdaRoleArn,ParameterValue=$lambdaRoleArn ParameterKey=LambdaS3Bucket,ParameterValue=$LambdaS3Bucket ParameterKey=FromEmailAddress,ParameterValue=$FromEmailAddress ParameterKey=DNS,ParameterValue=$DNS

export STACK_STATUS=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text)

while [ $STACK_STATUS != "CREATE_COMPLETE" ]
do
  STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`
done
echo "Created Stack ${STACK_NAME} successfully!"
