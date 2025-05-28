packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ec2" {
  region           = "ap-northeast-2"
  instance_type    = "t3.micro"
  ssh_username     = "ec2-user"
  ami_name         = "my-app-ami-{{timestamp}}"
  iam_instance_profile = "secret-role"
  vpc_id                  = "vpc-021af12dd2d6f3ad4"
  subnet_id               = "subnet-076fbfebb4cac7303" //public subnet
  associate_public_ip_address = true

  source_ami_filter {
    filters = {
      name                = "amzn2-ami-hvm-*-x86_64-gp2"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["amazon"]
  }

  user_data = file("user-data.sh") 

}

build {
  sources = ["source.amazon-ebs.ec2"]

provisioner "shell" {
  inline = [
    "sudo yum update -y",
    "sudo yum install -y docker curl",
    "sudo service docker start",
    "sudo usermod -a -G docker ec2-user",
    "mkdir -p /home/ec2-user/app",

    # Docker Compose v2 설치
    "mkdir -p ~/.docker/cli-plugins",
    "curl -SL https://github.com/docker/compose/releases/download/v2.27.1/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose",
    "chmod +x ~/.docker/cli-plugins/docker-compose",

    # Docker Compose 확인
    "docker compose version"
  ]
}

  provisioner "file" {
    source      = "../docker-compose.yml"
    destination = "/home/ec2-user/app/docker-compose.yml"
  }

  provisioner "file" {
    source      = "../fastapi_server"
    destination = "/home/ec2-user/app/fastapi-server"
  }

  provisioner "file" {
    source      = "../spring_server"
    destination = "/home/ec2-user/app/spring-server"
  }

  provisioner "shell" {
    inline = [
      "cd /home/ec2-user/app",
      "sudo docker compose build"
    ]
  }

}
