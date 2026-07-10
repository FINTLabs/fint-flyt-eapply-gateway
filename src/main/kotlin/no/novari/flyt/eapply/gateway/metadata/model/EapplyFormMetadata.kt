package no.novari.flyt.eapply.gateway.metadata.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Metadata that identifies an eApply form.")
data class EapplyFormMetadata(
    @field:NotBlank
    @field:Schema(
        description = "Source application integration id for the form.",
        example = "eapply-case",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val formId: String? = null,
    @field:NotBlank
    @field:Schema(
        description = "Human-readable form name.",
        example = "Application",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val formDisplayName: String? = null,
    @field:Schema(description = "Form metadata version.", example = "1")
    val version: Long? = null,
)
