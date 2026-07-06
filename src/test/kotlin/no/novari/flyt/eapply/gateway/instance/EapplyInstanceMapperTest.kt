package no.novari.flyt.eapply.gateway.instance

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstanceElement
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstanceMetadata
import no.novari.flyt.gateway.instance.model.MultipartFileReference
import no.novari.flyt.gateway.instance.model.instance.InstanceObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class EapplyInstanceMapperTest {
    private val objectMapper = jacksonObjectMapper()
    private val mapper = EapplyInstanceMapper()

    @Test
    fun `maps case instance values to instance object`() {
        val instance =
            EapplyInstance(
                metadata =
                    EapplyInstanceMetadata(
                        formId = "eapply-case-1234",
                        instanceId = "100401",
                    ),
                elements =
                    listOf(
                        element("Case.Title", "Soknad om redusert foreldrebetaling"),
                        element("Case.Type", "Barnehage"),
                        element("Applicant.Name", "Ola Normann"),
                        element("Applicant.NationalIdentityNumber", "00000000000"),
                    ),
            )

        val result =
            mapper.map(
                sourceApplicationId = 99L,
                incomingInstance = instance,
                persistFile = { error("No files should be persisted for case instances") },
            )

        assertEquals(
            InstanceObject(
                valuePerKey =
                    mapOf(
                        "Case.Title" to "Soknad om redusert foreldrebetaling",
                        "Case.Type" to "Barnehage",
                        "Applicant.Name" to "Ola Normann",
                        "Applicant.NationalIdentityNumber" to "00000000000",
                    ),
            ),
            result,
        )
    }

    @Test
    fun `maps journalpost multipart file values to multipart file references`() {
        val persistedFileReferences = mutableListOf<MultipartFileReference>()
        val instance =
            EapplyInstance(
                metadata =
                    EapplyInstanceMetadata(
                        formId = "eapply-journalpost-1234",
                        instanceId = "100402",
                    ),
                elements =
                    listOf(
                        element("Case.CaseNumber", "2025/12345"),
                        element("Journalpost.Title", "Soknad om redusert foreldrebetaling"),
                        element("Journalpost.Type", "INNGAENDE"),
                        element(
                            "MainDocument",
                            multipartFileValue(
                                fileName = "soknad.pdf",
                                mediaType = "application/pdf",
                                partName = "mainDocument",
                                originalFilename = "soknad.pdf",
                            ),
                        ),
                        element(
                            "Attachments",
                            listOf(
                                multipartFileValue(
                                    fileName = "inntektsdokumentasjon.pdf",
                                    mediaType = "application/pdf",
                                    partName = "attachments",
                                    originalFilename = "inntektsdokumentasjon.pdf",
                                ),
                                multipartFileValue(
                                    fileName = "arbeidskontrakt.pdf",
                                    mediaType = "application/pdf",
                                    partName = "attachments",
                                    originalFilename = "arbeidskontrakt.pdf",
                                ),
                            ),
                        ),
                    ),
            )

        val result =
            mapper.map(
                sourceApplicationId = 99L,
                incomingInstance = instance,
                persistFile = { fileReference ->
                    persistedFileReferences += fileReference
                    when (fileReference.fileName) {
                        "soknad.pdf" -> UUID.fromString("391e9177-2790-469a-9f42-c8042731bc55")
                        "inntektsdokumentasjon.pdf" -> UUID.fromString("dab3ecc8-2901-46f0-9553-2fbc3e71ae9e")
                        "arbeidskontrakt.pdf" -> UUID.fromString("5a15e2dd-29a7-41ac-a635-f4ab41d10d18")
                        else -> error("Unexpected file reference: $fileReference")
                    }
                },
            )

        assertEquals(
            InstanceObject(
                valuePerKey =
                    mapOf(
                        "Case.CaseNumber" to "2025/12345",
                        "Journalpost.Title" to "Soknad om redusert foreldrebetaling",
                        "Journalpost.Type" to "INNGAENDE",
                        "MainDocument.fileName" to "soknad.pdf",
                        "MainDocument.mediaType" to "application/pdf",
                        "MainDocument.file" to "391e9177-2790-469a-9f42-c8042731bc55",
                    ),
                objectCollectionPerKey =
                    mutableMapOf(
                        "Attachments" to
                            listOf(
                                InstanceObject(
                                    valuePerKey =
                                        mapOf(
                                            "fileName" to "inntektsdokumentasjon.pdf",
                                            "mediaType" to "application/pdf",
                                            "file" to "dab3ecc8-2901-46f0-9553-2fbc3e71ae9e",
                                        ),
                                ),
                                InstanceObject(
                                    valuePerKey =
                                        mapOf(
                                            "fileName" to "arbeidskontrakt.pdf",
                                            "mediaType" to "application/pdf",
                                            "file" to "5a15e2dd-29a7-41ac-a635-f4ab41d10d18",
                                        ),
                                ),
                            ),
                    ),
            ),
            result,
        )

        assertEquals(
            listOf(
                MultipartFileReference(
                    partName = "mainDocument",
                    fileName = "soknad.pdf",
                    originalFilename = "soknad.pdf",
                    type = MediaType.APPLICATION_PDF,
                ),
                MultipartFileReference(
                    partName = "attachments",
                    fileName = "inntektsdokumentasjon.pdf",
                    originalFilename = "inntektsdokumentasjon.pdf",
                    type = MediaType.APPLICATION_PDF,
                ),
                MultipartFileReference(
                    partName = "attachments",
                    fileName = "arbeidskontrakt.pdf",
                    originalFilename = "arbeidskontrakt.pdf",
                    type = MediaType.APPLICATION_PDF,
                ),
            ),
            persistedFileReferences,
        )
    }

    private fun element(
        id: String,
        value: Any?,
    ): EapplyInstanceElement {
        return EapplyInstanceElement(
            id = id,
            value = value.toJsonNode(),
        )
    }

    private fun Any?.toJsonNode(): JsonNode? {
        return objectMapper.valueToTree(this)
    }

    private fun multipartFileValue(
        fileName: String,
        mediaType: String,
        partName: String,
        originalFilename: String,
    ): Map<String, String> {
        return mapOf(
            "fileName" to fileName,
            "mediaType" to mediaType,
            "partName" to partName,
            "originalFilename" to originalFilename,
            "encoding" to "binary",
        )
    }
}
