#!/bin/bash

sudo dnf update -y
sudo dnf install -y awscli jq

APP_DIR="/home/ec2-user/app"

JWT_SECRET_KEY=$(aws secretsmanager get-secret-value --secret-id prod/api/jwt --query SecretString --output text)
OPENAI_SECRET_KEY=$(aws secretsmanager get-secret-value --secret-id prod/api/openai --query SecretString --output text)

cat <<EOF > $APP_DIR/.env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET_KEY=$JWT_SECRET_KEY
JWT_EXPIRATION=2592000000
OPENAI_SECRET_KEY=$OPENAI_SECRET_KEY
EOF

chown ec2-user:ec2-user $APP_DIR/.env

cd $APP_DIR
docker compose down || true
docker compose up -d
