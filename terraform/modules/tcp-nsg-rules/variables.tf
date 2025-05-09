variable "network_security_group_id" {}
variable "nsg_rules" {
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
}

