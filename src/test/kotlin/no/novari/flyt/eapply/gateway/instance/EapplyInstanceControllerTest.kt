package no.novari.flyt.eapply.gateway.instance

import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstanceElement
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstanceMetadata
import no.novari.flyt.gateway.instance.MultipartInstanceProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.web.multipart.MultipartFile

class EapplyInstanceControllerTest {
    private var processedAuthentication: Authentication? = null
    private var processedInstance: EapplyInstance? = null
    private var processedMultipartFiles: Collection<MultipartFile>? = null

    @Suppress("UNCHECKED_CAST")
    private val eapplyInstanceProcessor =
        mock(MultipartInstanceProcessor::class.java) { invocation ->
            if (invocation.method.name == "processInstance" && invocation.arguments.firstOrNull() is Authentication) {
                processedAuthentication = invocation.getArgument(0)
                processedInstance = invocation.getArgument(1)
                processedMultipartFiles = invocation.getArgument(2)
                ResponseEntity.ok().build<Void>()
            } else {
                Answers.RETURNS_DEFAULTS.answer(invocation)
            }
        } as MultipartInstanceProcessor<EapplyInstance>

    private val eapplyCaseStatusService = mock(EapplyCaseStatusService::class.java)
    private val authentication = mock(Authentication::class.java)
    private val controller =
        EapplyInstanceController(
            eapplyInstanceProcessor = eapplyInstanceProcessor,
            eapplyCaseStatusService = eapplyCaseStatusService,
        )

    @Test
    fun `passes request part instance and excludes it from multipart files`() {
        val instance =
            EapplyInstance(
                metadata =
                    EapplyInstanceMetadata(
                        formId = "eapply-journalpost",
                        instanceId = "instance-123",
                    ),
                elements =
                    listOf(
                        EapplyInstanceElement(
                            id = "Journalpost.Title",
                        ),
                    ),
            )
        val request =
            MockMultipartHttpServletRequest().apply {
                addFile(
                    MockMultipartFile(
                        "instance",
                        "instance.json",
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        """
                        {
                          "metadata": {
                            "formId": "eapply-journalpost",
                            "instanceId": "instance-123"
                          },
                          "elements": [
                            {
                              "id": "Journalpost.Title",
                              "value": "Soknad"
                            }
                          ]
                        }
                        """.trimIndent().toByteArray(),
                    ),
                )
                addFile(
                    MockMultipartFile(
                        "mainDocument",
                        "soknad.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        "pdf-content".toByteArray(),
                    ),
                )
            }

        controller.postMultipartInstance(instance, request, authentication)

        assertSame(authentication, processedAuthentication)
        assertEquals("eapply-journalpost", processedInstance?.metadata?.formId)
        assertEquals("instance-123", processedInstance?.metadata?.instanceId)
        assertEquals(listOf("mainDocument"), processedMultipartFiles?.map { it.name })
    }
}
