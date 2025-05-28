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
  ssh_username     = "ubuntu"
  ami_name         = "my-app-ami-{{timestamp}}"
  iam_instance_profile = "secret-role"
  vpc_id                  = "vpc-021af12dd2d6f3ad4"
  subnet_id               = "subnet-076fbfebb4cac7303" //public subnet
  associate_public_ip_address = true

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"]  
  }
  user_data = file("user-data.sh") 
}

build {
  sources = ["source.amazon-ebs.ec2"]

provisioner "shell" {
  inline = [
    "sudo apt-get update -y",
    "sudo apt-get install -y docker.io curl",

    "sudo systemctl start docker",
    "sudo usermod -aG docker ubuntu",
    "mkdir -p /home/ubuntu/app",

    "mkdir -p ~/.docker/cli-plugins",
    "curl -SL https://github.com/docker/compose/releases/download/v2.27.1/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose",
    "chmod +x ~/.docker/cli-plugins/docker-compose",

    "docker compose version"
  ]
}

  provisioner "file" {
    source      = "../docker-compose.yml"
    destination = "/home/ubuntu/app/docker-compose.yml"
  }

  provisioner "file" {
    source      = "../fastapi_server"
    destination = "/home/ubuntu/app/fastapi-server"
  }

  provisioner "file" {
    source      = "../spring_server"
    destination = "/home/ubuntu/app/spring-server"
  }

  provisioner "shell" {
    inline = [
      "cd /home/ubuntu/app",
      "sudo docker-compose build"
    ]
  }

}
