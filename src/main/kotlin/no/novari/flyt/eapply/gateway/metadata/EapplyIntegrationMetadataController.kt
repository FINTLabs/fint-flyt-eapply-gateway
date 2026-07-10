package no.novari.flyt.eapply.gateway.metadata

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.eapply.gateway.OPEN_API_BEARER_AUTH_SCHEME
import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormDefinition
import no.novari.flyt.gateway.metadata.IntegrationMetadataProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

@Tag(name = "Metadata", description = "Receive eApply form definitions as Flyt integration metadata.")
@SecurityRequirement(name = OPEN_API_BEARER_AUTH_SCHEME)
@RestController
@RequestMapping("$EXTERNAL_API/eapply/metadata")
class EapplyIntegrationMetadataController(
    private val eapplyFormDefinitionMapper: EapplyFormDefinitionMapper,
    private val eapplyFormDefinitionValidator: EapplyFormDefinitionValidator,
    private val integrationMetadataProcessor: IntegrationMetadataProcessor,
) {
    @Operation(
        summary = "Submit form metadata",
        description = "Receives an eApply form definition and maps it to Flyt integration metadata.",
        requestBody =
            OpenApiRequestBody(
                required = true,
                description = "eApply form definition.",
                content =
                    [
                        Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = Schema(implementation = EapplyFormDefinition::class),
                            examples = [ExampleObject(value = METADATA_REQUEST_EXAMPLE)],
                        ),
                    ],
            ),
        responses =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Metadata accepted for processing.",
                    content = [Content()],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid form definition.",
                    content = [Content()],
                ),
                ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid bearer token.",
                    content = [Content()],
                ),
                ApiResponse(
                    responseCode = "403",
                    description = "Source application is not authorized.",
                    content = [Content()],
                ),
            ],
    )
    @PostMapping
    fun postMetadata(
        @RequestBody eapplyFormDefinition: EapplyFormDefinition,
        @Parameter(hidden = true)
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return integrationMetadataProcessor.processIntegrationMetadata(
            authentication = authentication,
            incomingMetadata = eapplyFormDefinition,
            integrationMetadataMapper = eapplyFormDefinitionMapper,
            integrationMetadataValidator = eapplyFormDefinitionValidator,
        )
    }

    private companion object {
        private const val METADATA_REQUEST_EXAMPLE =
            """
            {
              "metadata": {
                "formId": "eapply-case",
                "formDisplayName": "Application",
                "version": 1
              },
              "elements": [
                {
                  "id": "Case.Title",
                  "displayName": "Title",
                  "type": "String"
                }
              ]
            }
            """
    }
}
