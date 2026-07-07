package no.novari.flyt.eapply.gateway.instance

import jakarta.validation.Valid
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.gateway.instance.MultipartInstanceProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@RestController
@RequestMapping("$EXTERNAL_API/eapply/instances")
class EapplyInstanceController(
    private val eapplyInstanceProcessor: MultipartInstanceProcessor<EapplyInstance>,
) {
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
