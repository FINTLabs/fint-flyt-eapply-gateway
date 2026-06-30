package no.novari.flyt.example.gateway.instance.advanced

import no.novari.flyt.example.gateway.instance.model.AdvancedExample
import no.novari.flyt.example.gateway.instance.model.Dokument
import no.novari.flyt.example.gateway.instance.model.Filinnhold
import no.novari.flyt.example.gateway.instance.model.Saksbehandler
import no.novari.flyt.gateway.webinstance.model.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AdvancedMappingServiceTest {
    private val service = AdvancedMappingService()

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `maps advanced example with documents and handlers`() {
        val fileBytes = byteArrayOf(1, 2, 3)
        val dokument =
            Dokument(
                tittel = "Original tittel",
                fil =
                    Filinnhold(
                        filnavn = "vedlegg.pdf",
                        mimeType = "application/pdf",
                        base64 = fileBytes,
                    ),
            )
        val saksbehandler = Saksbehandler(epost = "saks@example.com", navn = "Saksbehandler")
        val input =
            AdvancedExample(
                sysId = "sys-adv-1",
                string1 = "hello",
                int1 = 7,
                dokumenter = listOf(dokument),
                saksbehandlere = listOf(saksbehandler),
            )

        val capturedFiles = mutableListOf<File>()
        val expectedFileId = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")

        val result =
            service.map(
                sourceApplicationId = 99L,
                incomingInstance = input,
                persistFile = { file ->
                    capturedFiles += file
                    expectedFileId
                },
            )

        assertEquals(1, capturedFiles.size)
        val capturedFile = capturedFiles.single()
        assertEquals("vedlegg.pdf", capturedFile.name)
        assertEquals(99L, capturedFile.sourceApplicationId)
        assertEquals("sys-adv-1", capturedFile.sourceApplicationInstanceId)
        assertEquals(MediaType.APPLICATION_PDF, capturedFile.type)
        assertEquals("UTF-8", capturedFile.encoding)
        assertEquals(Base64.encode(fileBytes), capturedFile.base64Contents)

        assertEquals(
            mapOf(
                "sysId" to "sys-adv-1",
                "string1" to "hello",
                "int1" to "7",
            ),
            result.valuePerKey,
        )

        val dokumenterObjects = result.objectCollectionPerKey.getValue("dokumenter")
        val saksbehandlerObjects = result.objectCollectionPerKey.getValue("saksbehandlere")

        assertEquals(1, dokumenterObjects.size)
        assertEquals(1, saksbehandlerObjects.size)

        val dokumentObject = dokumenterObjects.single()
        assertEquals(
            mapOf(
                "tittel" to "vedlegg.pdf",
                "filnavn" to "vedlegg.pdf",
                "mediatype" to "application/pdf",
                "fil" to expectedFileId.toString(),
            ),
            dokumentObject.valuePerKey,
        )

        val saksbehandlerObject = saksbehandlerObjects.single()
        assertEquals(
            mapOf(
                "epost" to "saks@example.com",
                "navn" to "Saksbehandler",
            ),
            saksbehandlerObject.valuePerKey,
        )

        assertTrue(dokumentObject.objectCollectionPerKey.isEmpty())
        assertTrue(saksbehandlerObject.objectCollectionPerKey.isEmpty())
    }
}
