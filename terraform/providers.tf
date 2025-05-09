provider "oci" {
  region           = var.oci_region
  tenancy_ocid     = var.oci_tenancy_id_ocid
  user_ocid        = var.oci_user_id_ocid
  fingerprint      = var.oci_fingerprint_ocid
  private_key_path = var.private_key_path
}