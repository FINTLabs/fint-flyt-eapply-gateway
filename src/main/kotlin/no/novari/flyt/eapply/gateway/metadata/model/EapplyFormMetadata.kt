package no.novari.flyt.eapply.gateway.metadata.model

import jakarta.validation.constraints.NotBlank

data class EapplyFormMetadata(
    @field:NotBlank
    val formId: String? = null,
    @field:NotBlank
    val formDisplayName: String? = null,
    val version: Long? = null,
)
