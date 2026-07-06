package no.novari.flyt.eapply.gateway.instance.model

import jakarta.validation.constraints.NotBlank

data class EapplyInstanceMetadata(
    @field:NotBlank
    val formId: String? = null,
    @field:NotBlank
    val instanceId: String? = null,
)
