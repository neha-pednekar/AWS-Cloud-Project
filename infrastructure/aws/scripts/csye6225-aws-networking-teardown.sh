STACK_NAME=$1
VPC_NAME=${STACK_NAME}-csye6225-vpc
SUBNET1_NAME=${STACK_NAME}-csye6225-subnet1
SUBNET2_NAME=${STACK_NAME}-csye6225-subnet2
SUBNET3_NAME=${STACK_NAME}-csye6225-subnet3
SUBNET4_NAME=${STACK_NAME}-csye6225-subnet4
SUBNET5_NAME=${STACK_NAME}-csye6225-subnet5
SUBNET6_NAME=${STACK_NAME}-csye6225-subnet6
IG_NAME=${STACK_NAME}-csye6225-InternetGateway
ROUTE_TABLE_NAME=${STACK_NAME}-csye6225-public-route-table

export VPC_ID=$(aws ec2 describe-vpcs --query 'Vpcs[*].[VpcId, Tags[0].Value]' --output text | grep $VPC_NAME | awk '{print $1}')
export SUBNET1_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET1_NAME --output text | awk '{print $9}')
export SUBNET2_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET2_NAME --output text | awk '{print $9}')
export SUBNET3_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET3_NAME --output text | awk '{print $9}')
export SUBNET4_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET4_NAME --output text | awk '{print $9}')
export SUBNET5_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET5_NAME --output text | awk '{print $9}')
export SUBNET6_ID=$(aws ec2 describe-subnets --filters Name=tag:Name,Values=$SUBNET6_NAME --output text | awk '{print $9}')
export IG_ID=$(aws ec2 describe-internet-gateways --query 'InternetGateways[*].[InternetGatewayId, Tags[0].Value]' --output text | grep $IG_NAME | awk '{print $1}')
export ROUTE_TABLE_ID=$(aws ec2 describe-route-tables --query 'RouteTables[*].[RouteTableId, Tags[0].Value]' --output text | grep $ROUTE_TABLE_NAME | awk '{print $1}')

echo "Detaching Internet Gateway ${IG_NAME} from VPC ${VPC_NAME}"
aws ec2 detach-internet-gateway --internet-gateway-id $IG_ID --vpc-id $VPC_ID
if [ $? -eq "0" ]
then 
	echo "Internet Gateway detached from VPC successfully!"
else
	echo "Detachment of Internet Gateway from VPC failed"
	exit 1
fi

echo "Deleting Internet Gateway"
aws ec2 delete-internet-gateway --internet-gateway-id $IG_ID
if [ $? -eq "0" ]
then 
	echo "Internet Gateway deleted successfully!"
else
	echo "Deletion of Internet Gateway failed"
	exit 1
fi

echo "Deleting Public Route"
aws ec2 delete-route --route-table-id ${ROUTE_TABLE_ID} --destination-cidr-block 0.0.0.0/0
if [ $? -eq "0" ]
then 
	echo "Public Route deleted successfully!"
else
	echo "Deletion of Public Route failed"
	exit 1
fi

echo "Deleting Subnet1 ${SUBNET1_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET1_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet1 ${SUBNET1_NAME} deleted successfully!"
else
	echo "Deletion of Subnet1 ${SUBNET1_NAME} failed"
	exit 1
fi

echo "Deleting Subnet2 ${SUBNET2_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET2_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet2 ${SUBNET2_NAME} deleted successfully!"
else
	echo "Deletion of Subnet2 ${SUBNET2_NAME} failed"
	exit 1
fi

echo "Deleting Subnet3 ${SUBNET3_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET3_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet3 ${SUBNET3_NAME} deleted successfully!"
else
	echo "Deletion of Subnet3 ${SUBNET3_NAME} failed"
	exit 1
fi

echo "Deleting Subnet4 ${SUBNET4_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET4_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet4 ${SUBNET4_NAME} deleted successfully!"
else
	echo "Deletion of Subnet4 ${SUBNET4_NAME} failed"
	exit 1
fi

echo "Deleting Subnet5 ${SUBNET5_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET5_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet5 ${SUBNET5_NAME} deleted successfully!"
else
	echo "Deletion of Subnet5 ${SUBNET5_NAME} failed"
	exit 1
fi

echo "Deleting Subnet6 ${SUBNET6_NAME}"
aws ec2 delete-subnet --subnet-id ${SUBNET6_ID}
if [ $? -eq "0" ]
then 
	echo "Subnet6 ${SUBNET6_NAME} deleted successfully!"
else
	echo "Deletion of Subnet6 ${SUBNET6_NAME} failed"
	exit 1
fi

echo "Deleting Public Route Table ${ROUTE_TABLE_NAME}"
aws ec2 delete-route-table --route-table-id ${ROUTE_TABLE_ID}
if [ $? -eq "0" ]
then 
	echo "Public Route table deleted successfully!"
else
	echo "Deletion of Public Route table failed"
	exit 1
fi

echo "Deleting VPC ${VPC_NAME}"
aws ec2 delete-vpc --vpc-id ${VPC_ID}
if [ $? -eq "0" ]
then 
	echo "VPC ${VPC_NAME} deleted successfully!"
else
	echo "Deletion of VPC ${VPC_NAME} failed"
	exit 1
fi

if [ $? -ne "0" ]
then 
	echo "Termination of AWS CLI Stack failed"
else
	echo "Termination of AWS CLI Stack Success"
fi
