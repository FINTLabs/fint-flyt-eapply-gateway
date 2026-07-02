package no.novari.flyt.eapply.gateway.metadata.model.eapply

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class EapplyFormElement(
    val id: String? = null,
    @field:NotBlank
    val displayName: String? = null,
    @field:NotBlank
    val type: String? = null,
    val multiple: Boolean = false,
    @field:Valid
    val elements: List<@Valid EapplyFormElement>? = null,
)
