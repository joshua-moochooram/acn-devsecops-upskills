resource "tls_private_key" "public_private_key_pair" {
  algorithm = "RSA"
}

module "dev-compartment" {
  source       = "./modules/compartment"
  description  = "ACN CI/CD Upskilling"
  name         = "acn-upskills"
  tenancy_ocid = var.oci_tenancy_id_ocid
}

module "dev-vcn" {
  source           = "./modules/vcn"
  cidr_block       = "10.0.0.0/16"
  compartment_ocid = module.dev-compartment.id
  display_name     = "acn-vcn"
  dns_label        = "iacacnnet"
}

module "public_subnet" {
  source                     = "./modules/subnet"
  cidr_block                 = "10.0.1.0/24"
  display_name               = "acn_public_subnet"
  vcn_id                     = module.dev-vcn.id
  route_table_id             = oci_core_default_route_table.default_route_table.id #module.public_route_table.id
  security_list_ids          = [module.dev-vcn.default_security_list_id]
  dhcp_options_id            = module.dev-vcn.default_dhcp_options_id
  dns_label                  = "wp"
  compartment_ocid           = module.dev-compartment.id
  prohibit_public_ip_on_vnic = false
  availability_domain        = data.oci_identity_availability_domain.ad.name
}

module "oci-cicd-vm" {
  source                                          = "./modules/flex-instance"
  vcn_id                                          = module.dev-vcn.id
  subnet_id                                       = module.public_subnet.id
  nsg_ids                                         = [module.oci_app_network_sec_group.nsg_id]
  compartment_ocid                                = module.dev-compartment.id
  ssh_public_key                                  = var.generate_public_ssh_key ? tls_private_key.public_private_key_pair.public_key_openssh : join("\n", [var.ssh_public_key, tls_private_key.public_private_key_pair.public_key_openssh])
  display_name                                    = "acn-cicd-vm"
  region                                          = var.oci_region
  availability_domain                             = data.oci_identity_availability_domain.ad.name
  instance_shape                                  = "VM.Standard.A1.Flex"
  instance_shape_config_baseline_ocpu_utilization = "BASELINE_1_1"
  instance_shape_config_memory_in_gbs             = "12"
  instance_shape_config_ocpus                     = "2"
  instance_shape_config_vcpus                     = "2"

  user_data = base64encode(templatefile("${path.module}/script/userdata/cicd-server.sh",
    {
      name = "oci-cicd-vm"
      password = var.user_password
    }))

  preserve_boot_volume   = false
  instance_image_ocid    = var.instance_image_ocid[var.oci_region]
  assign_public_ip       = true
}

module "oci-app-vm" {
  source                                          = "./modules/flex-instance"
  vcn_id                                          = module.dev-vcn.id
  subnet_id                                       = module.public_subnet.id
  nsg_ids                                         = [module.oci_app_network_sec_group.nsg_id]
  compartment_ocid                                = module.dev-compartment.id
  ssh_public_key                                  = var.generate_public_ssh_key ? tls_private_key.public_private_key_pair.public_key_openssh : join("\n", [var.ssh_public_key, tls_private_key.public_private_key_pair.public_key_openssh])
  display_name                                    = "oci-app-vm"
  region                                          = var.oci_region
  availability_domain                             = data.oci_identity_availability_domain.ad.name
  instance_shape                                  = "VM.Standard.A1.Flex"
  instance_shape_config_baseline_ocpu_utilization = "BASELINE_1_1"
  instance_shape_config_memory_in_gbs             = "12"
  instance_shape_config_ocpus                     = "2"
  instance_shape_config_vcpus                     = "2"

  user_data              = base64encode(templatefile("${path.module}/script/userdata/init-bootstrap-server.sh",
    {
      name = "oci-app-vm"
      password = var.user_password
    }))
  preserve_boot_volume   = false
  instance_image_ocid    = var.instance_image_ocid[var.oci_region]
  assign_public_ip       = true
}

module "jenkins-internet_gateway" {
  source           = "./modules/internet-gateway"
  compartment_ocid = module.dev-compartment.id
  display_name     = "jenkins"
  enabled          = true
  vcn_id           = module.dev-vcn.id
  route_table_id   = oci_core_default_route_table.default_route_table.id
}

resource "oci_core_default_route_table" "default_route_table" {
  manage_default_resource_id = module.dev-vcn.default_route_table_id
  display_name               = "IaCDefaultRouteTable"

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = module.jenkins-internet_gateway.id
  }
}

module "oci_app_network_sec_group" {
  source           = "./modules/network-security-groups"
  compartment_ocid = module.dev-compartment.id
  nsg_display_name = "${var.oci_app_name}-nsg"
  nsg_whitelist_ip = "0.0.0.0/0"
  vcn_id           = module.dev-vcn.id
  vcn_cidr_block   = "0.0.0.0/0"
}

module "nsg_security_rules" {
  source                    = "./modules/tcp-nsg-rules"
  network_security_group_id  = module.oci_app_network_sec_group.nsg_id
  nsg_rules = var.nsg_security_rules
}

resource "time_sleep" "wait_5_minutes" {
  depends_on = [module.oci-cicd-vm]
  create_duration = "7m"
}

locals {
  private_key = tls_private_key.public_private_key_pair.private_key_pem
  cicd_ip = module.oci-cicd-vm.oci_flex_vm_public_ip
  app_ip = module.oci-app-vm.oci_flex_vm_public_ip
}

resource "null_resource" "cicd_provisioner" {
  depends_on = [module.oci-cicd-vm, time_sleep.wait_5_minutes]

  provisioner "file" {
    content     = local.private_key
    destination = "/home/ubuntu/private"

    connection {
      type        = "ssh"
      host        = local.cicd_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }
  }

  provisioner "file" {
    content     = file("${path.module}/script/maven.yml")
    destination = "/home/ubuntu/maven.yml"

    connection {
      type        = "ssh"
      host        = local.cicd_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }
  }

  provisioner "file" {
    content     = file("${path.module}/script/sonarqube.yml")
    destination = "/home/ubuntu/sonarqube.yml"

    connection {
      type        = "ssh"
      host        = local.cicd_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }
  }

  # Execute commands
  provisioner "remote-exec" {
    connection {
      type        = "ssh"
      host        = local.cicd_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }

    inline = [
      "sudo ansible-playbook /home/ubuntu/maven.yml",
      "sudo docker-compose -f /home/ubuntu/sonarqube.yml up -d",
    ]
  }
}

resource "null_resource" "app-server-config" {
  depends_on = [time_sleep.wait_5_minutes]

  provisioner "file" {
    content     = local.private_key
    destination = "/home/ubuntu/private"

    connection {
      type        = "ssh"
      host        = local.app_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }
  }

  provisioner "file" {
    content     = file("${path.module}/script/maven.yml")
    destination = "/home/ubuntu/maven.yml"

    connection {
      type        = "ssh"
      host        = local.app_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }
  }

  provisioner "remote-exec" {
    connection {
      type        = "ssh"
      host        = local.app_ip
      agent       = false
      timeout     = "5m"
      user        = "ubuntu"
      private_key = local.private_key
    }

    inline = ["sudo ansible-playbook /home/ubuntu/maven.yml"]
  }
}

#resource "time_sleep" "wait_2_minutes" {
#  create_duration = "2m"
#}

#resource "null_resource" "jenkins_Default_Password" {
#  depends_on = [time_sleep.wait_2_minutes]
#
#  provisioner "remote-exec" {
#    connection {
#      type        = "ssh"
#      host        = local.cicd_ip
#      agent       = false
#      timeout     = "5m"
#      user        = "ubuntu"
#      private_key = local.private_key
#    }
#
#    inline = ["sudo cat /var/lib/jenkins/secrets/initialAdminPassword"]
#  }
#}

#resource "null_resource" "ArgoCD_Default_Password" {
#  depends_on = [time_sleep.wait_2_minutes]
#
#  provisioner "remote-exec" {
#    connection {
#      type        = "ssh"
#      host        = local.app_ip
#      agent       = false
#      timeout     = "5m"
#      user        = "ubuntu"
#      private_key = local.private_key
#    }
#
#    inline = ["kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d"]
#  }
#}

#resource "null_resource" "forward_ArgoCD_Default_Password" {
#  depends_on = [null_resource.ArgoCD_Default_Password]
#
#  provisioner "remote-exec" {
#    connection {
#      type        = "ssh"
#      host        = local.app_ip
#      agent       = false
#      timeout     = "5m"
#      user        = "ubuntu"
#      private_key = local.private_key
#    }
#
#    inline = ["kubectl port-forward svc/argocd-server -n argocd --address 0.0.0.0 8088:443 >> /dev/null 2>&1 &"]
#  }
#}

