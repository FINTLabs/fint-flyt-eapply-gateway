package no.novari.flyt.eapply.gateway.instance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.novari.flyt.eapply.gateway.OPEN_API_BEARER_AUTH_SCHEME
import no.novari.flyt.eapply.gateway.instance.model.CaseStatus
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.eapply.gateway.instance.model.EapplyMultipartInstanceRequestDocumentation
import no.novari.flyt.gateway.instance.MultipartInstanceProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.server.ResponseStatusException
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

private const val INSTANCE_PART_NAME = "instance"

@Tag(name = "Instances", description = "Receive eApply instances and retrieve case status.")
@SecurityRequirement(name = OPEN_API_BEARER_AUTH_SCHEME)
@RestController
@RequestMapping("$EXTERNAL_API/eapply/instances")
class EapplyInstanceController(
    private val eapplyInstanceProcessor: MultipartInstanceProcessor<EapplyInstance>,
    private val eapplyCaseStatusService: EapplyCaseStatusService,
) {
    @Operation(
        summary = "Get case status",
        description = "Returns the archive case id for an eApply instance when a Flyt case exists.",
        responses =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Case status found.",
                    content =
                        [
                            Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = Schema(implementation = CaseStatus::class),
                                examples = [ExampleObject(value = """{"archiveCaseId":"2026/456"}""")],
                            ),
                        ],
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
                ApiResponse(
                    responseCode = "404",
                    description = "Case status was not found.",
                    content = [Content()],
                ),
            ],
    )
    @GetMapping("{sourceApplicationInstanceId}/status")
    fun getCaseStatus(
        @Parameter(hidden = true)
        authentication: Authentication,
        @Parameter(
            description = "Source application instance id from eApply.",
            example = "instance-123",
        )
        @PathVariable sourceApplicationInstanceId: String,
    ): ResponseEntity<CaseStatus> =
        eapplyCaseStatusService
            .getCaseStatus(authentication, sourceApplicationInstanceId)
            ?.let { ResponseEntity.ok(it) }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Case with sourceApplicationInstanceId=$sourceApplicationInstanceId could not be found",
            )

    @Operation(
        summary = "Submit instance",
        description = "Receives an eApply instance as JSON and maps it to a Flyt instance.",
        requestBody =
            OpenApiRequestBody(
                required = true,
                description = "eApply instance payload.",
                content =
                    [
                        Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = Schema(implementation = EapplyInstance::class),
                            examples = [ExampleObject(value = INSTANCE_REQUEST_EXAMPLE)],
                        ),
                    ],
            ),
        responses =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Instance accepted for processing.",
                    content = [Content()],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid instance payload.",
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
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postInstance(
        @Valid @RequestBody eapplyInstance: EapplyInstance,
        @Parameter(hidden = true)
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return eapplyInstanceProcessor.processInstance(authentication, eapplyInstance, emptyList())
    }

    @Operation(
        summary = "Submit multipart instance",
        description =
            "Receives an eApply instance in the multipart part named `instance`. Additional file parts can " +
                "use any part name referenced from `elements.value.partName`.",
        requestBody =
            OpenApiRequestBody(
                required = true,
                description = "Multipart eApply instance payload with optional files.",
                content =
                    [
                        Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = Schema(implementation = EapplyMultipartInstanceRequestDocumentation::class),
                            encoding =
                                [
                                    Encoding(
                                        name = INSTANCE_PART_NAME,
                                        contentType = MediaType.APPLICATION_JSON_VALUE,
                                    ),
                                ],
                            examples = [ExampleObject(value = MULTIPART_REQUEST_EXAMPLE)],
                        ),
                    ],
            ),
        responses =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Multipart instance accepted for processing.",
                    content = [Content()],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid multipart instance payload.",
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
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun postMultipartInstance(
        @Valid @RequestPart(INSTANCE_PART_NAME) eapplyInstance: EapplyInstance,
        @Parameter(hidden = true)
        multipartRequest: MultipartHttpServletRequest,
        @Parameter(hidden = true)
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return eapplyInstanceProcessor.processInstance(
            authentication = authentication,
            incomingInstance = eapplyInstance,
            multipartFiles =
                multipartRequest.multiFileMap
                    .filterKeys { it != INSTANCE_PART_NAME }
                    .values
                    .flatten(),
        )
    }

    private companion object {
        private const val INSTANCE_REQUEST_EXAMPLE =
            """
            {
              "metadata": {
                "formId": "eapply-case",
                "instanceId": "instance-123"
              },
              "elements": [
                {
                  "id": "Case.Title",
                  "value": "Application title"
                }
              ]
            }
            """

        private const val MULTIPART_REQUEST_EXAMPLE =
            """
            {
              "instance": {
                "metadata": {
                  "formId": "eapply-journalpost",
                  "instanceId": "instance-123"
                },
                "elements": [
                  {
                    "id": "MainDocument",
                    "value": {
                      "fileName": "application.pdf",
                      "mediaType": "application/pdf",
                      "partName": "mainDocument",
                      "originalFilename": "application.pdf",
                      "encoding": "binary"
                    }
                  }
                ]
              },
              "mainDocument": "<binary>"
            }
            """
    }
}
