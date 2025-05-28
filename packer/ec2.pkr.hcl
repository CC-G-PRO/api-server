packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ec2" {
  region                        = "ap-northeast-2"
  instance_type                 = "t3.micro"
  ssh_username                  = "ubuntu"
  ami_name                      = "my-app-ami-{{timestamp}}"
  iam_instance_profile          = "secret-role"
  vpc_id                        = "vpc-021af12dd2d6f3ad4"
  subnet_id                     = "subnet-076fbfebb4cac7303"
  associate_public_ip_address   = true

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

  # Docker 및 Compose Plugin 설치
  provisioner "shell" {
    inline = [
      "sudo rm -rf /var/lib/apt/lists/*",
      "sudo apt-get clean",
      "sudo apt-get update -y",
      "sudo apt-get install -y ca-certificates curl gnupg",

      "sudo install -m 0755 -d /etc/apt/keyrings",
      "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg",
      "sudo chmod a+r /etc/apt/keyrings/docker.gpg",

      "echo \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable\" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",
      "sudo apt-get update -y",

      "sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
      "sudo systemctl enable docker",
      "sudo systemctl start docker",
      "sudo usermod -aG docker ubuntu",
      "mkdir -p /home/ubuntu/app",

      "docker compose version"
    ]
  }

  provisioner "file" {
    source      = "../docker-compose.yml"
    destination = "/home/ubuntu/app/docker-compose.yml"
  }

  provisioner "file" {
    source      = "../fastapi_server"
    destination = "/home/ubuntu/app/fastapi_server"
  }

  provisioner "file" {
    source      = "../spring_server"
    destination = "/home/ubuntu/app/spring_server"
  }

  provisioner "shell" {
    inline = [
      "cd /home/ubuntu/app",
      "sudo docker compose build"
    ]
  }
}
