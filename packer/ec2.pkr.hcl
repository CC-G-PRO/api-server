variable "bastion_key_path" {}
variable "bastion_user" {}
variable "bastion_host" {}


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
  subnet_id               = "subnet-076fbfebb4cac7303"

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
      "sudo yum install -y docker docker-compose",
      "sudo service docker start",
      "sudo usermod -a -G docker ec2-user",
      "mkdir -p /home/ec2-user/app"
    ]
  }

  provisioner "file" {
    source      = "../docker-compose.yml"
    destination = "/home/ec2-user/app/docker-compose.yml"
  }

}
