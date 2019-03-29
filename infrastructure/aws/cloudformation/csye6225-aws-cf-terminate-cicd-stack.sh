STACK_NAME=$1

DOMAINNAME=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text)
DNS=${DOMAINNAME::-1}
echo $DNS

bucketName=code-deploy.$DNS
echo $bucketName
if [ -n "$bucketName"]
then
aws s3 rm s3://$bucketName/ --recursive
fi

aws cloudformation delete-stack --stack-name $STACK_NAME

aws cloudformation wait stack-delete-complete --stack-name $STACK_NAME

if [ $? -ne "0" ]
then 
	echo "Deletion of Stack failed"
else
	echo "Deletion of Stack Success"
fi
