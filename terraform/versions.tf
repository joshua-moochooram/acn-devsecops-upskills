terraform {
  required_providers {
    oci = {
      source  = "hashicorp/oci"
      version = ">= 6.31.0"
    }
    random = {
      source = "hashicorp/random"
    }
  }
  required_version = ">= 1.2.0"
}