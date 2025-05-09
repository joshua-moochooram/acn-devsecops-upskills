variable "generate_public_ssh_key" {
  default = false
}

variable "private_key_path" {
}

variable "availability_domain_name" {
  default = null
}

variable "oci_tenancy_id_ocid" {
  type = string
}

variable "oci_user_id_ocid" {
  type = string
}

variable "oci_fingerprint_ocid" {
  type = string
}

variable "oci_region" {
  type = string
}

variable "user_password" {}

variable "oci_app_name" {
  default = "oci-jenkins-vm"
}

variable "instance_image_ocid" {
  type = map(string)
  default = {
    us-phoenix-1   = "ocid1.image.oc1.phx.aaaaaaaa6hooptnlbfwr5lwemqjbu3uqidntrlhnt45yihfj222zahe7p3wq"
    us-ashburn-1   = "ocid1.image.oc1.iad.aaaaaaaa6tp7lhyrcokdtf7vrbmxyp2pctgg4uxvt4jz4vc47qoc2ec4anha"
    eu-frankfurt-1 = "ocid1.image.oc1.eu-frankfurt-1.aaaaaaaadvi77prh3vjijhwe5xbd6kjg3n5ndxjcpod6om6qaiqeu3csof7a"
    uk-london-1    = "ocid1.image.oc1.uk-london-1.aaaaaaaaw5gvriwzjhzt2tnylrfnpanz5ndztyrv3zpwhlzxdbkqsjfkwxaq"
    eu-marseille-1-aarch64 = "ocid1.image.oc1.eu-marseille-1.aaaaaaaa457toyb4brqmdbcjdvjchspijbpjb3rubfddf7cgqrpfmjp55txq" # ubuntu aarch64
    eu-marseille-1 = "ocid1.image.oc1.eu-marseille-1.aaaaaaaaqihfeepadhdma7udc7n2vlfmienfwim4vl53dkftvfikrlxfi3ca" # good ubuntu
  }
}

variable "ssh_public_key" {}

variable "nsg_security_rules" {
  description = "A list of NSG rules with dynamic values"
  type = list(object({
    description                = string
    destination_min_port_range = string
    destination_max_port_range = string
    protocol                  = string
    direction                 = string
    source_range              = string
    stateless                 = bool
  }))
  default = [
    {
      description                = "8080-8089 Ingress"
      destination_min_port_range = "8080"
      destination_max_port_range = "8089"
      protocol                   = "6"   # TCP protocol
      direction                 = "INGRESS"
      source_range              = "0.0.0.0/0"
      stateless                 = false
    },
    {
      description                = "443 Ingress"
      destination_min_port_range = "443"
      destination_max_port_range = "443"
      protocol                   = "6"   # TCP protocol
      direction                 = "INGRESS"
      source_range              = "0.0.0.0/0"
      stateless                 = false
    },
    {
      description                = "80 Ingress"
      destination_min_port_range = "80"
      destination_max_port_range = "80"
      protocol                   = "6"   # TCP protocol
      direction                 = "INGRESS"
      source_range              = "0.0.0.0/0"
      stateless                 = false
    },
    {
      description                = "8443 Ingress"
      destination_min_port_range = "8443"
      destination_max_port_range = "8443"
      protocol                   = "6"   # TCP protocol
      direction                 = "INGRESS"
      source_range              = "0.0.0.0/0"
      stateless                 = false
    },
    {
      description                = "22 Ingress"
      destination_min_port_range = "22"
      destination_max_port_range = "22"
      protocol                   = "6"
      direction                 = "INGRESS"
      source_range              = "0.0.0.0/0"
      stateless                 = false
    }
  ]
}
