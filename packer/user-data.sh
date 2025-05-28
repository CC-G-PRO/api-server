#!/bin/bash
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "===== Starting User Data Script ====="

sudo yum update -y
sudo yum install -y awscli jq docker

sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

AWS_REGION="ap-northeast-2"
APP_DIR="/home/ec2-user/app"

echo "===== Fetching Secrets from AWS Secrets Manager ====="

OPENAI_SECRET_JSON=$(aws --region "$AWS_REGION" secretsmanager get-secret-value --secret-id prod/api/openai --query SecretString --output text)
OPENAI_SECRET_KEY=$(echo "$OPENAI_SECRET_JSON" | jq -r '.OPENAI_SECRET_KEY')

JWT_SECRET_JSON=$(aws --region "$AWS_REGION" secretsmanager get-secret-value --secret-id prod/api/jwt --query SecretString --output text)
JWT_SECRET_KEY=$(echo "$JWT_SECRET_JSON" | jq -r '.JWT_SECRET_KEY')

# Save raw JSON to files
echo "$OPENAI_SECRET_JSON" > /home/ec2-user/openai_secret.json
echo "$JWT_SECRET_JSON" > /home/ec2-user/jwt_secret.json
chown ec2-user:ec2-user /home/ec2-user/*_secret.json

echo "===== Secret Values ====="
echo "OPENAI_SECRET_KEY: $OPENAI_SECRET_KEY"
echo "JWT_SECRET_KEY: $JWT_SECRET_KEY"

echo "===== Exporting as Environment Variables ====="
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET_KEY=$JWT_SECRET_KEY
export JWT_EXPIRATION=2592000000
export OPENAI_SECRET_KEY=$OPENAI_SECRET_KEY

echo "===== Environment Variables Exported ====="
env | grep -E 'SPRING_PROFILES_ACTIVE|JWT_SECRET_KEY|OPENAI_SECRET_KEY|JWT_EXPIRATION'

echo "===== Running Application ====="

mkdir -p "$APP_DIR"
cd "$APP_DIR"

docker-compose down || true
docker-compose up -d

echo "===== User Data Script Completed ====="
