package no.novari.flyt.eapply.gateway.instance

import jakarta.validation.Valid
import no.novari.flyt.eapply.gateway.instance.model.CaseStatus
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
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
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$EXTERNAL_API/eapply/instances")
class EapplyInstanceController(
    private val eapplyInstanceProcessor: MultipartInstanceProcessor<EapplyInstance>,
    private val eapplyCaseStatusService: EapplyCaseStatusService,
) {
    @GetMapping("{sourceApplicationInstanceId}/status")
    fun getCaseStatus(
        authentication: Authentication,
        @PathVariable sourceApplicationInstanceId: String,
    ): ResponseEntity<CaseStatus> =
        eapplyCaseStatusService
            .getCaseStatus(authentication, sourceApplicationInstanceId)
            ?.let { ResponseEntity.ok(it) }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Case with sourceApplicationInstanceId=$sourceApplicationInstanceId could not be found",
            )

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postInstance(
        @Valid @RequestBody eapplyInstance: EapplyInstance,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return eapplyInstanceProcessor.processInstance(authentication, eapplyInstance, emptyList())
    }

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun postMultipartInstance(
        @Valid @RequestPart("instance") eapplyInstance: EapplyInstance,
        multipartRequest: MultipartHttpServletRequest,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        return eapplyInstanceProcessor.processInstance(
            authentication = authentication,
            incomingInstance = eapplyInstance,
            multipartFiles = multipartRequest.toMultipartFiles(),
        )
    }

    private fun MultipartHttpServletRequest.toMultipartFiles(): Collection<MultipartFile> {
        return multiFileMap.values.flatten()
    }
}
