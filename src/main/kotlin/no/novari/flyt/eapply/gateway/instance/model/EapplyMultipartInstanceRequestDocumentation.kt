package no.novari.flyt.eapply.gateway.instance.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "EapplyMultipartInstanceRequest",
    description = "Documentation schema for multipart eApply instance requests.",
)
data class EapplyMultipartInstanceRequestDocumentation(
    @field:Schema(
        description = "eApply instance JSON. The multipart part name must be `instance`.",
        implementation = EapplyInstance::class,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val instance: EapplyInstance? = null,
    @field:Schema(
        description = "Example binary file part referenced by `elements.value.partName`.",
        type = "string",
        format = "binary",
    )
    val mainDocument: String? = null,
)
