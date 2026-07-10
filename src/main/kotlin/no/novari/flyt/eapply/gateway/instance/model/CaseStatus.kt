package no.novari.flyt.eapply.gateway.instance.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Status for a source application instance.")
data class CaseStatus(
    @field:Schema(
        description = "Archive case id for the Flyt case.",
        example = "2026/456",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val archiveCaseId: String,
)
