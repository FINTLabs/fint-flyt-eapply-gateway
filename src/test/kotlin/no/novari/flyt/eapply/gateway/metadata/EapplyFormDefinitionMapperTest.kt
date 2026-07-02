package no.novari.flyt.eapply.gateway.metadata

import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormDefinition
import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormElement
import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormMetadata
import no.novari.flyt.eapply.gateway.metadata.model.fint.InstanceMetadataCategory
import no.novari.flyt.eapply.gateway.metadata.model.fint.InstanceMetadataContent
import no.novari.flyt.eapply.gateway.metadata.model.fint.InstanceObjectCollectionMetadata
import no.novari.flyt.eapply.gateway.metadata.model.fint.InstanceValueMetadata
import no.novari.flyt.eapply.gateway.metadata.model.fint.IntegrationMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EapplyFormDefinitionMapperTest {
    private val mapper = EapplyFormDefinitionMapper()

    @Test
    fun `maps case metadata to integration metadata`() {
        val formDefinition =
            EapplyFormDefinition(
                metadata =
                    EapplyFormMetadata(
                        formId = "eapply-case-1234",
                        formDisplayName = "sak - soknad 1234",
                        version = 1,
                    ),
                elements =
                    listOf(
                        stringElement("Case.Title", "Sakstittel"),
                        stringElement("Case.Type", "Sakstype"),
                        stringElement("Applicant.NationalIdentityNumber", "Fodselsnummer"),
                        stringElement("Applicant.Name", "Navn"),
                    ),
            )

        val result = mapper.toIntegrationMetadata(99L, formDefinition)

        assertEquals(
            IntegrationMetadata(
                sourceApplicationId = 99L,
                sourceApplicationIntegrationId = "eapply-case-1234",
                sourceApplicationIntegrationUri = null,
                integrationDisplayName = "sak - soknad 1234",
                version = 1,
                instanceMetadata =
                    InstanceMetadataContent(
                        instanceValueMetadata =
                            listOf(
                                stringValue("Case.Title", "Sakstittel"),
                                stringValue("Case.Type", "Sakstype"),
                                stringValue("Applicant.NationalIdentityNumber", "Fodselsnummer"),
                                stringValue("Applicant.Name", "Navn"),
                            ),
                    ),
            ),
            result,
        )
    }

    @Test
    fun `maps journalpost metadata with document groups to integration metadata`() {
        val formDefinition =
            EapplyFormDefinition(
                metadata =
                    EapplyFormMetadata(
                        formId = "eapply-journalpost-1234",
                        formDisplayName = "journalpost - soknad 1234",
                        version = 1,
                    ),
                elements =
                    listOf(
                        stringElement("Case.CaseNumber", "Saksnummer"),
                        stringElement("Journalpost.Title", "Journalposttittel"),
                        stringElement("Journalpost.Type", "Journalposttype"),
                        group(
                            id = "MainDocument",
                            displayName = "Hoveddokument",
                            elements =
                                listOf(
                                    stringElement("fileName", "Filnavn"),
                                    stringElement("mediaType", "MIME-type"),
                                    fileElement("base64", "Filinnhold"),
                                ),
                        ),
                        group(
                            id = "Attachments",
                            displayName = "Vedlegg",
                            multiple = true,
                            elements =
                                listOf(
                                    stringElement("fileName", "Filnavn"),
                                    stringElement("mediaType", "MIME-type"),
                                    fileElement("base64", "Filinnhold"),
                                ),
                        ),
                    ),
            )

        val result = mapper.toIntegrationMetadata(99L, formDefinition)

        assertEquals(
            IntegrationMetadata(
                sourceApplicationId = 99L,
                sourceApplicationIntegrationId = "eapply-journalpost-1234",
                sourceApplicationIntegrationUri = null,
                integrationDisplayName = "journalpost - soknad 1234",
                version = 1,
                instanceMetadata =
                    InstanceMetadataContent(
                        instanceValueMetadata =
                            listOf(
                                stringValue("Case.CaseNumber", "Saksnummer"),
                                stringValue("Journalpost.Title", "Journalposttittel"),
                                stringValue("Journalpost.Type", "Journalposttype"),
                            ),
                        instanceObjectCollectionMetadata =
                            listOf(
                                InstanceObjectCollectionMetadata(
                                    displayName = "Vedlegg",
                                    key = "Attachments",
                                    objectMetadata =
                                        InstanceMetadataContent(
                                            instanceValueMetadata =
                                                listOf(
                                                    stringValue("fileName", "Filnavn"),
                                                    stringValue("mediaType", "MIME-type"),
                                                    fileValue("base64", "Filinnhold"),
                                                ),
                                        ),
                                ),
                            ),
                        categories =
                            listOf(
                                InstanceMetadataCategory(
                                    displayName = "Hoveddokument",
                                    content =
                                        InstanceMetadataContent(
                                            instanceValueMetadata =
                                                listOf(
                                                    stringValue("MainDocument.fileName", "Filnavn"),
                                                    stringValue("MainDocument.mediaType", "MIME-type"),
                                                    fileValue("MainDocument.base64", "Filinnhold"),
                                                ),
                                        ),
                                ),
                            ),
                    ),
            ),
            result,
        )
    }

    private fun stringElement(
        id: String,
        displayName: String,
    ): EapplyFormElement {
        return EapplyFormElement(id = id, displayName = displayName, type = "String")
    }

    private fun fileElement(
        id: String,
        displayName: String,
    ): EapplyFormElement {
        return EapplyFormElement(id = id, displayName = displayName, type = "File")
    }

    private fun group(
        id: String,
        displayName: String,
        multiple: Boolean = false,
        elements: List<EapplyFormElement>,
    ): EapplyFormElement {
        return EapplyFormElement(
            id = id,
            displayName = displayName,
            type = "Group",
            multiple = multiple,
            elements = elements,
        )
    }

    private fun stringValue(
        key: String,
        displayName: String,
    ): InstanceValueMetadata {
        return InstanceValueMetadata(
            key = key,
            displayName = displayName,
            type = InstanceValueMetadata.Type.STRING,
        )
    }

    private fun fileValue(
        key: String,
        displayName: String,
    ): InstanceValueMetadata {
        return InstanceValueMetadata(
            key = key,
            displayName = displayName,
            type = InstanceValueMetadata.Type.FILE,
        )
    }
}
