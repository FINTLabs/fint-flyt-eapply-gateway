package no.novari.flyt.eapply.gateway.instance.model

import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotBlank

data class EapplyInstanceElement(
    @field:NotBlank
    val id: String? = null,
    val value: JsonNode? = null,
)
