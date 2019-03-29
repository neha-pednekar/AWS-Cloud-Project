STACK_NAME=$1
EC2_NAME=${STACK_NAME}-csye6225-ec2


DOMAINNAME=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text)
DNS=${DOMAINNAME#csye6225-fall2018-}
echo $DNS

bucketName="${DNS}csye6225.com"
echo $bucketName

if [ -n "$bucketName"]
then
aws s3 rm s3://$bucketName/ --recursive
fi

export ec2InstanceId=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId, State.Name, Tags[*][?Value==`${EC2_NAME}`]]' --output text|grep running|awk '{print $1}')
if [ -n "$ec2InstanceId" ]
then
	aws ec2 stop-instances --instance-ids $ec2InstanceId
	aws ec2 wait instance-stopped --instance-ids $ec2InstanceId
	echo "Instance ${ec2InstanceId} is stopped!!"

	aws ec2 modify-instance-attribute --no-disable-api-termination --instance-id $ec2InstanceId
	echo "Termination protection is removed from the instance!!"
fi

aws cloudformation delete-stack --stack-name $STACK_NAME
aws cloudformation wait stack-delete-complete --stack-name $STACK_NAME

if [ $? -eq 0 ]
then
	echo "Stack ${STACK_NAME} deleted successfully!!"
else
	echo "Unable to delete stack. Please input correct name!!"
fi