package no.novari.flyt.eapply.gateway.metadata.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EapplyFormDefinition(
    @field:NotNull
    @field:Valid
    val metadata: EapplyFormMetadata? = null,
    @field:NotEmpty
    @field:Valid
    val elements: List<@Valid EapplyFormElement>? = null,
)
