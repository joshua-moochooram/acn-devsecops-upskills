resource "oci_core_network_security_group_security_rule" "rule" {
  for_each = { for idx, rule in var.nsg_rules : idx => rule }

  network_security_group_id = var.network_security_group_id
  protocol                  = each.value.protocol
  description               = each.value.description
  direction                 = each.value.direction
  source                    = each.value.source_range
  stateless                 = each.value.stateless

  tcp_options {
    destination_port_range {
      min = each.value.destination_min_port_range
      max = each.value.destination_max_port_range
    }
  }
}

