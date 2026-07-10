package no.novari.flyt.eapply.gateway.instance.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "eApply instance that is mapped to a Flyt instance.")
data class EapplyInstance(
    @field:NotNull
    @field:Valid
    @field:Schema(
        description = "Instance-level metadata.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val metadata: EapplyInstanceMetadata? = null,
    @field:NotEmpty
    @field:Valid
    @field:Schema(
        description = "Instance values keyed by eApply element ids.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val elements: List<@Valid EapplyInstanceElement>? = null,
)
