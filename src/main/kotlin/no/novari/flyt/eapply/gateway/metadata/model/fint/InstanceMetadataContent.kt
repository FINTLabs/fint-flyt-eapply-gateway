package no.novari.flyt.eapply.gateway.metadata.model.fint

data class InstanceMetadataContent(
    val instanceValueMetadata: List<InstanceValueMetadata> = emptyList(),
    val instanceObjectCollectionMetadata: List<InstanceObjectCollectionMetadata> = emptyList(),
    val categories: List<InstanceMetadataCategory> = emptyList(),
)
