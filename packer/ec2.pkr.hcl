packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ec2" {
  region                      = "ap-northeast-2"
  instance_type               = "t2.micro"
  ssh_username                = "ec2-user"
  ami_name                    = "my-app-ami-{{timestamp}}"
  iam_instance_profile        = "secret-role"
  vpc_id                      = "vpc-021af12dd2d6f3ad4"
  subnet_id                   = "subnet-076fbfebb4cac7303"
  associate_public_ip_address = true

  source_ami_filter {
    filters = {
      name                = "amzn2-ami-hvm-*-x86_64-gp2"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["137112412989"]
  }

  user_data = file("user-data.sh")
}

build {
  sources = ["source.amazon-ebs.ec2"]

  provisioner "shell" {
    inline = [
      "sudo yum update -y",
      "sudo amazon-linux-extras install docker",
      "sudo service docker start",
      "sudo usermod -a -G docker ec2-user",
      "sudo chkconfig docker on",
      "sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose",
      "sudo chmod +x /usr/local/bin/docker-compose",
      "docker-compose version",

      "mkdir -p /home/ec2-user/app"
    ]
  }

  provisioner "file" {
    source      = "../docker-compose.yml"
    destination = "/home/ec2-user/app/docker-compose.yml"
  }

  provisioner "file" {
    source      = "../fastapi_server"
    destination = "/home/ec2-user/app/fastapi_server"
  }

  provisioner "file" {
    source      = "../spring_server"
    destination = "/home/ec2-user/app/spring_server"
  }

  provisioner "shell" {
    inline = [
      "cd /home/ec2-user/app",
      "docker-compose build"
    ]
  }
}
