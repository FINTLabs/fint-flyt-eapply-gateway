package no.novari.flyt.eapply.gateway.metadata

import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormDefinition
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$EXTERNAL_API/eapply/metadata")
class EapplyIntegrationMetadataController(
    private val eapplyFormDefinitionMapper: EapplyFormDefinitionMapper,
    private val eapplyFormDefinitionValidator: EapplyFormDefinitionValidator,
    private val integrationMetadataProducerService: IntegrationMetadataProducerService,
    private val sourceApplicationAuthorizationService: SourceApplicationAuthorizationService,
) {
    @PostMapping("/cases")
    fun postCaseMetadata(
        @RequestBody eapplyFormDefinition: EapplyFormDefinition,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return postIntegrationMetadata(eapplyFormDefinition, authentication)
    }

    @PostMapping("/journalposts")
    fun postJournalpostMetadata(
        @RequestBody eapplyFormDefinition: EapplyFormDefinition,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return postIntegrationMetadata(eapplyFormDefinition, authentication)
    }

    private fun postIntegrationMetadata(
        eapplyFormDefinition: EapplyFormDefinition,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        eapplyFormDefinitionValidator.validate(eapplyFormDefinition)?.let { validationErrors ->
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation error(s): ${validationErrors.map { "'$it'" }}",
            )
        }

        val integrationMetadata =
            eapplyFormDefinitionMapper.toIntegrationMetadata(
                sourceApplicationAuthorizationService.getSourceApplicationId(authentication),
                eapplyFormDefinition,
            )

        integrationMetadataProducerService.publishNewIntegrationMetadata(integrationMetadata)

        return ResponseEntity.accepted().build()
    }
}
