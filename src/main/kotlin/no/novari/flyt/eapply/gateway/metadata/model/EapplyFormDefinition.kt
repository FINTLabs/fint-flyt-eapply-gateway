package no.novari.flyt.eapply.gateway.metadata.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "eApply form definition that is mapped to Flyt integration metadata.")
data class EapplyFormDefinition(
    @field:NotNull
    @field:Valid
    @field:Schema(
        description = "Form-level metadata.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val metadata: EapplyFormMetadata? = null,
    @field:NotEmpty
    @field:Valid
    @field:Schema(
        description = "Top-level form elements. Nested group elements are represented through child elements.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val elements: List<@Valid EapplyFormElement>? = null,
)
