#!/bin/bash
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "===== Starting User Data Script ====="

sudo yum update -y
sudo yum install -y awscli jq docker

sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

AWS_REGION="ap-northeast-2"
APP_DIR="/home/ec2-user/app"

OPENAI_SECRET_JSON=$(aws --region $AWS_REGION secretsmanager get-secret-value --secret-id prod/api/openai --query SecretString --output text)
OPENAI_SECRET_KEY=$(echo "$OPENAI_SECRET_JSON" | jq -r '.OPENAI_SECRET_KEY')

JWT_SECRET_JSON=$(aws --region $AWS_REGION secretsmanager get-secret-value --secret-id prod/api/jwt --query SecretString --output text)
JWT_SECRET_KEY=$(echo "$JWT_SECRET_JSON" | jq -r '.JWT_SECRET_KEY')

SPRING_DB_SECRET_JSON=$(aws --region "$AWS_REGION" secretsmanager get-secret-value --secret-id prod/spring-db-config --query SecretString --output text)

SPRING_DATASOURCE_URL=$(echo "$SPRING_DB_SECRET_JSON" | jq -r '.SPRING_DATASOURCE_URL')
SPRING_DATASOURCE_USERNAME=$(echo "$SPRING_DB_SECRET_JSON" | jq -r '.SPRING_DATASOURCE_USERNAME')
SPRING_DATASOURCE_PASSWORD=$(echo "$SPRING_DB_SECRET_JSON" | jq -r '.SPRING_DATASOURCE_PASSWORD')


# .env 생성
cat <<EOF > $APP_DIR/.env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET_KEY=$JWT_SECRET_KEY
JWT_EXPIRATION=2592000000
OPENAI_SECRET_KEY=$OPENAI_SECRET_KEY
SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME=$SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD=$SPRING_DATASOURCE_PASSWORD
EOF

chown ec2-user:ec2-user $APP_DIR/.env

cd $APP_DIR
docker-compose down || true
docker-compose up -d

echo "===== User Data Script Completed ====="
