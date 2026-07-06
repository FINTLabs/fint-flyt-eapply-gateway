package no.novari.flyt.eapply.gateway.instance.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EapplyInstance(
    @field:NotNull
    @field:Valid
    val metadata: EapplyInstanceMetadata? = null,
    @field:NotEmpty
    @field:Valid
    val elements: List<@Valid EapplyInstanceElement>? = null,
)
