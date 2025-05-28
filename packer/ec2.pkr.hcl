packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ec2" {
  # source 설정...
}

build {
  sources = ["source.amazon-ebs.ec2"]

  provisioner "shell" {
    inline = [
      "sudo dnf update -y",
      "sudo dnf swap -y curl-minimal curl",
      "sudo dnf install -y ca-certificates dnf-plugins-core",
      "sudo curl -fsSL https://download.docker.com/linux/centos/docker-ce.repo -o /etc/yum.repos.d/docker-ce.repo",
      "sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin",
      "sudo systemctl enable docker",
      "sudo systemctl start docker",
      "sudo usermod -aG docker ec2-user",
      "mkdir -p /home/ec2-user/app",
      "docker compose version"
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
      "sudo docker compose build"
    ]
  }
}
