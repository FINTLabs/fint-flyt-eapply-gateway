package no.novari.flyt.eapply.gateway.metadata

import jakarta.validation.Validation
import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormDefinition
import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormElement
import no.novari.flyt.eapply.gateway.metadata.model.eapply.EapplyFormMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EapplyFormDefinitionValidatorTest {
    private val validator =
        EapplyFormDefinitionValidator(
            Validation.buildDefaultValidatorFactory(),
        )

    @Test
    fun `accepts duplicate child ids in different groups`() {
        val formDefinition =
            validFormDefinition(
                elements =
                    listOf(
                        group(
                            id = "MainDocument",
                            elements = listOf(stringElement("fileName")),
                        ),
                        group(
                            id = "Attachments",
                            multiple = true,
                            elements = listOf(stringElement("fileName")),
                        ),
                    ),
            )

        assertNull(validator.validate(formDefinition))
    }

    @Test
    fun `rejects missing ids and duplicate sibling ids`() {
        val formDefinition =
            validFormDefinition(
                elements =
                    listOf(
                        stringElement("Case.Title"),
                        stringElement("Case.Title"),
                        EapplyFormElement(displayName = "Uten id", type = "String"),
                    ),
            )

        assertEquals(
            listOf(
                "Missing element ID(s) for: [Uten id]",
                "Duplicate element ID(s) under root: [Case.Title]",
            ),
            validator.validate(formDefinition),
        )
    }

    @Test
    fun `rejects multiple non-group elements and child elements on value elements`() {
        val formDefinition =
            validFormDefinition(
                elements =
                    listOf(
                        EapplyFormElement(
                            id = "Applicant.Name",
                            displayName = "Navn",
                            type = "String",
                            multiple = true,
                        ),
                        EapplyFormElement(
                            id = "Case.Title",
                            displayName = "Sakstittel",
                            type = "String",
                            elements = listOf(stringElement("Nested")),
                        ),
                    ),
            )

        assertEquals(
            listOf(
                "Element(s) with multiple=true must be type Group: [Applicant.Name]",
                "Only group elements can contain child elements: [Case.Title]",
            ),
            validator.validate(formDefinition),
        )
    }

    private fun validFormDefinition(elements: List<EapplyFormElement>): EapplyFormDefinition {
        return EapplyFormDefinition(
            metadata =
                EapplyFormMetadata(
                    formId = "eapply-case-1234",
                    formDisplayName = "sak - soknad 1234",
                    version = 1,
                ),
            elements = elements,
        )
    }

    private fun stringElement(id: String): EapplyFormElement {
        return EapplyFormElement(
            id = id,
            displayName = id,
            type = "String",
        )
    }

    private fun group(
        id: String,
        multiple: Boolean = false,
        elements: List<EapplyFormElement>,
    ): EapplyFormElement {
        return EapplyFormElement(
            id = id,
            displayName = id,
            type = "Group",
            multiple = multiple,
            elements = elements,
        )
    }
}
