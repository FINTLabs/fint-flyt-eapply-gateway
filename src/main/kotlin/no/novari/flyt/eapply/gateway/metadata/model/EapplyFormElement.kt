package no.novari.flyt.eapply.gateway.metadata.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@Schema(description = "eApply form element. Group elements can contain nested elements.")
data class EapplyFormElement(
    @field:Schema(
        description = "Element id. Required by gateway validation and unique within the same sibling level.",
        example = "Case.Title",
    )
    val id: String? = null,
    @field:NotBlank
    @field:Schema(
        description = "Human-readable element name.",
        example = "Title",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val displayName: String? = null,
    @field:NotBlank
    @field:Schema(
        description = "Element type. File maps to a Flyt file value. Bool and Boolean map to boolean values.",
        allowableValues = ["String", "File", "Bool", "Boolean", "Group"],
        example = "String",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val type: String? = null,
    @field:Schema(
        description = "Whether this element can occur multiple times. Only Group elements can be multiple.",
        example = "false",
    )
    val multiple: Boolean = false,
    @field:Valid
    @field:Schema(description = "Nested child elements. Only Group elements can contain children.")
    val elements: List<@Valid EapplyFormElement>? = null,
)
