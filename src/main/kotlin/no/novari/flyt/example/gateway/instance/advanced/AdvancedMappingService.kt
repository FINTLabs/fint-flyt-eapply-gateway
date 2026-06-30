package no.novari.flyt.example.gateway.instance.advanced

import no.novari.flyt.example.gateway.instance.mapping.putOrEmpty
import no.novari.flyt.example.gateway.instance.model.AdvancedExample
import no.novari.flyt.example.gateway.instance.model.Dokument
import no.novari.flyt.example.gateway.instance.model.Saksbehandler
import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class AdvancedMappingService : InstanceMapper<AdvancedExample> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: AdvancedExample,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        return with(incomingInstance) {
            val dokumenterInstanceObjects =
                mapAttachmentDocumentsToInstanceObjects(
                    persistFile = persistFile,
                    sourceApplicationId = sourceApplicationId,
                    sourceApplicationInstanceId = incomingInstance.sysId,
                    dokumenter = dokumenter,
                )

            val saksbehandlerInstanceObjects =
                mapSaksbehandlerToInstanceObjects(
                    saksbehandlere = saksbehandlere,
                )

            val valuePerKey =
                buildMap {
                    putOrEmpty("sysId", sysId)
                    putOrEmpty("string1", string1)
                    putOrEmpty("int1", int1)
                }

            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "dokumenter" to dokumenterInstanceObjects,
                    "saksbehandlere" to saksbehandlerInstanceObjects,
                )

            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapSaksbehandlerToInstanceObjects(saksbehandlere: List<Saksbehandler>): List<InstanceObject> {
        return saksbehandlere.map(::mapSaksBehandlerToInstanceObject)
    }

    private fun mapSaksBehandlerToInstanceObject(saksbehandler: Saksbehandler): InstanceObject {
        return InstanceObject(
            valuePerKey =
                buildMap {
                    putOrEmpty("epost", saksbehandler.epost)
                    putOrEmpty("navn", saksbehandler.navn)
                },
        )
    }

    private fun mapAttachmentDocumentsToInstanceObjects(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        dokumenter: List<Dokument>,
    ): List<InstanceObject> {
        return dokumenter.map {
            mapAttachmentDocumentToInstanceObject(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                dokument = it,
            )
        }
    }

    private fun getMediaTypeFromFilnavn(filnavn: String): MediaType {
        val mediaType = MediaTypeFactory.getMediaType(filnavn)
        return mediaType.orElseThrow {
            IllegalArgumentException("No media type found for fileName=$filnavn")
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun toFile(
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        dokument: Dokument,
        mediaType: MediaType,
    ): File {
        return File(
            name = dokument.fil.filnavn,
            type = mediaType,
            sourceApplicationId = sourceApplicationId,
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            encoding = "UTF-8",
            base64Contents = Base64.encode(dokument.fil.base64),
        )
    }

    private fun mapAttachmentDocumentToInstanceObject(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        dokument: Dokument,
    ): InstanceObject {
        val mediaType = getMediaTypeFromFilnavn(dokument.fil.filnavn)
        val file = toFile(sourceApplicationId, sourceApplicationInstanceId, dokument, mediaType)
        val fileId = persistFile(file)
        return mapAttachmentDocumentAndFileIdToInstanceObject(dokument, mediaType, fileId)
    }

    private fun mapAttachmentDocumentAndFileIdToInstanceObject(
        dokument: Dokument,
        mediaType: MediaType,
        fileId: UUID,
    ): InstanceObject {
        return InstanceObject(
            valuePerKey =
                buildMap {
                    putOrEmpty("tittel", dokument.fil.filnavn)
                    putOrEmpty("filnavn", dokument.fil.filnavn)
                    putOrEmpty("mediatype", mediaType.toString())
                    putOrEmpty("fil", fileId)
                },
        )
    }
}
