package no.novari.flyt.eapply.gateway.metadata

import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormDefinition
import no.novari.flyt.gateway.metadata.IntegrationMetadataProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$EXTERNAL_API/eapply/metadata")
class EapplyIntegrationMetadataController(
    private val eapplyFormDefinitionMapper: EapplyFormDefinitionMapper,
    private val eapplyFormDefinitionValidator: EapplyFormDefinitionValidator,
    private val integrationMetadataProcessor: IntegrationMetadataProcessor,
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
        return integrationMetadataProcessor.processIntegrationMetadata(
            authentication = authentication,
            incomingMetadata = eapplyFormDefinition,
            integrationMetadataMapper = eapplyFormDefinitionMapper,
            integrationMetadataValidator = eapplyFormDefinitionValidator,
        )
    }
}
