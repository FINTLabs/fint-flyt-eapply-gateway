package no.novari.flyt.eapply.gateway.instance.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Metadata that identifies an eApply instance.")
data class EapplyInstanceMetadata(
    @field:NotBlank
    @field:Schema(
        description = "Source application integration id for the form.",
        example = "eapply-case",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val formId: String? = null,
    @field:NotBlank
    @field:Schema(
        description = "Source application instance id.",
        example = "instance-123",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val instanceId: String? = null,
)
