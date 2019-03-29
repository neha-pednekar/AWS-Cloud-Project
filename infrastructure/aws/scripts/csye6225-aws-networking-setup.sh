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

export VPC_ID=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 --instance-tenancy default --query 'Vpc.VpcId' --output text)

if [ $? -eq 0 ]
then 
	aws ec2 create-tags --resources $VPC_ID --tags Key=Name,Value=$VPC_NAME
	echo "VPC created ${VPC_NAME} successfully!"
	
	echo "Creating Subnet1..."
	export SUBNET1_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1a" --cidr-block 10.0.1.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 1 ${SUBNET1_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET1_ID --tags Key=Name,Value=$SUBNET1_NAME
	else
		echo "Creation of Subnet 1 Failed"
		exit 1
	fi

	echo "Creating Subnet2..."
	export SUBNET2_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1b" --cidr-block 10.0.2.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 2 ${SUBNET2_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET2_ID --tags Key=Name,Value=$SUBNET2_NAME
	else
		echo "Creation of Subnet 2 Failed"
		exit 1
	fi

	echo "Creating Subnet3..."
	export SUBNET3_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1c" --cidr-block 10.0.3.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 3 ${SUBNET3_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET3_ID --tags Key=Name,Value=$SUBNET3_NAME
	else
		echo "Creation of Subnet 3 Failed"
		exit 1
	fi

	echo "Creating Subnet4..."
	export SUBNET4_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1a" --cidr-block 10.0.4.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 4 ${SUBNET4_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET4_ID --tags Key=Name,Value=$SUBNET4_NAME
	else
		echo "Creation of Subnet 4 Failed"
		exit 1
	fi

	echo "Creating Subnet5..."
	export SUBNET5_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1b" --cidr-block 10.0.5.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 5 ${SUBNET5_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET5_ID --tags Key=Name,Value=$SUBNET5_NAME
	else
		echo "Creation of Subnet 5 Failed"
		exit 1
	fi

	echo "Creating Subnet6..."
	export SUBNET6_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --availability-zone "us-east-1c" --cidr-block 10.0.6.0/24 --query 'Subnet.SubnetId' --output text)
	if [ $? -eq 0 ]
	then
		echo "Subnet 6 ${SUBNET6_NAME} Created successfully!"
		aws ec2 create-tags --resources $SUBNET6_ID --tags Key=Name,Value=$SUBNET6_NAME
	else
		echo "Creation of Subnet 6 Failed"
		exit 1
	fi

	echo "Creating Internet Gateway"
	export IG_ID=$(aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text)
	echo $IG_ID
	if [ $? -eq 0 ]
	then
		echo "Internet Gateway ${IG_NAME} created successfully!"
		aws ec2 create-tags --resources $IG_ID --tags Key=Name,Value=$IG_NAME
	else
		echo "Creation of Internet Gateway Failed"
		exit 1
	fi

	echo "Attaching Internet Gateway to VPC"
	aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IG_ID
	if [ $? -eq 0 ]
	then
		echo "Internet gateway attached to VPC successfully!"
	else
		echo "Internet gateway attached to VPC failed"
		exit 1
	fi

	echo "Creating a public route table"
	export ROUTE_TABLE_ID=$(aws ec2 create-route-table --vpc-id $VPC_ID --query "RouteTable.RouteTableId" --output text)
	if [ $? -eq 0 ]
	then
		echo "Public Route Table ${ROUTE_TABLE_NAME} created successfully!"
		aws ec2 create-tags --resources $ROUTE_TABLE_ID --tags Key=Name,Value=$ROUTE_TABLE_NAME
	else
		echo "Creation of Route Table failed"
		exit 1
	fi

	echo "Attaching subnets to Public Route Table"
	aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_ID --subnet-id $SUBNET1_ID
	if [ $? -eq 0 ]
	then
		echo "Attached Subnet1 ${SUBNET1_NAME} to Public Route Table ${ROUTE_TABLE_NAME} successfully!"
	else
		echo "Failed to attach Subnet1 ${SUBNET1_NAME} to Route Table ${ROUTE_TABLE_NAME} failed"
		exit 1
	fi
	aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_ID --subnet-id $SUBNET2_ID
	if [ $? -eq 0 ]
	then
		echo "Attached Subnet2 ${SUBNET2_NAME} to Public Route Table ${ROUTE_TABLE_NAME} successfully!"
	else
		echo "Failed to attach Subnet2 ${SUBNET2_NAME} to Route Table ${ROUTE_TABLE_NAME} failed"
		exit 1
	fi
	aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_ID --subnet-id $SUBNET3_ID
	if [ $? -eq 0 ]
	then
		echo "Attached Subnet3 ${SUBNET3_NAME} to Public Route Table ${ROUTE_TABLE_NAME} successfully!"
	else
		echo "Failed to attach Subnet3 ${SUBNET3_NAME} to Route Table ${ROUTE_TABLE_NAME} failed"
		exit 1
	fi

	echo "Creating a public route in the public route table"
	aws ec2 create-route --route-table-id $ROUTE_TABLE_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IG_ID
	if [ $? -eq 0 ]
	then
		echo "Public Route created successfully!"
	else
		echo "Creation of Route failed"
		exit 1
	fi

else
	echo "Creation of VPC Failed"
	exit 1
fi
