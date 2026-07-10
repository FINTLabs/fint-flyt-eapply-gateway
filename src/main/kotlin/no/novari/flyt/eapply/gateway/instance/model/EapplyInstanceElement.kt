package no.novari.flyt.eapply.gateway.instance.model

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "eApply instance element value.")
data class EapplyInstanceElement(
    @field:NotBlank
    @field:Schema(
        description = "Element id from the form definition.",
        example = "Case.Title",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: String? = null,
    @field:Schema(
        description =
            "Element value. Scalars map to Flyt values, objects map to nested keys, arrays map to object " +
                "collections, and file objects can reference multipart parts through `partName`.",
        example = "\"Application title\"",
    )
    val value: JsonNode? = null,
)
