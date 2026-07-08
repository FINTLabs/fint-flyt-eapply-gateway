package no.novari.flyt.eapply.gateway.metadata

import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormDefinition
import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormElement
import no.novari.flyt.gateway.metadata.IntegrationMetadataValidator
import org.springframework.stereotype.Service

@Service
class EapplyFormDefinitionValidator(
    validatorFactory: ValidatorFactory,
) : IntegrationMetadataValidator<EapplyFormDefinition> {
    private val fieldValidator: Validator = validatorFactory.validator

    override fun validate(incomingMetadata: EapplyFormDefinition): List<String>? {
        val errors =
            fieldValidator
                .validate(incomingMetadata)
                .map { constraintViolation ->
                    "${constraintViolation.propertyPath} ${constraintViolation.message}"
                }.sorted()
                .toMutableList()

        errors += validateElements(incomingMetadata.elements.orEmpty())

        return errors.takeIf { it.isNotEmpty() }
    }

    private fun validateElements(
        elements: List<EapplyFormElement>,
        parentPath: List<String> = emptyList(),
    ): List<String> {
        val errors = mutableListOf<String>()

        val missingElementIds =
            elements
                .filter { it.id.isNullOrBlank() }
                .map { element -> buildDisplayPath(parentPath, element) }
        if (missingElementIds.isNotEmpty()) {
            errors += "Missing element ID(s) for: $missingElementIds"
        }

        val duplicateElementIds =
            elements
                .mapNotNull { it.id?.takeIf(String::isNotBlank) }
                .duplicates()
        if (duplicateElementIds.isNotEmpty()) {
            errors += "Duplicate element ID(s) under ${parentPath.toErrorPath()}: $duplicateElementIds"
        }

        val multipleNonGroupElements =
            elements
                .filter { it.multiple && !isGroupElement(it) }
                .map { element -> buildDisplayPath(parentPath, element) }
        if (multipleNonGroupElements.isNotEmpty()) {
            errors += "Element(s) with multiple=true must be type Group: $multipleNonGroupElements"
        }

        val nonGroupElementsWithChildren =
            elements
                .filter { !isGroupElement(it) && !it.elements.isNullOrEmpty() }
                .map { element -> buildDisplayPath(parentPath, element) }
        if (nonGroupElementsWithChildren.isNotEmpty()) {
            errors += "Only group elements can contain child elements: $nonGroupElementsWithChildren"
        }

        elements
            .filter { !it.elements.isNullOrEmpty() }
            .forEach { element ->
                errors +=
                    validateElements(
                        elements = element.elements.orEmpty(),
                        parentPath = parentPath + (element.id ?: element.displayName ?: UNKNOWN_ELEMENT),
                    )
            }

        return errors
    }

    private fun List<String>.duplicates(): List<String> {
        val seen = mutableSetOf<String>()
        return filterNot(seen::add).distinct()
    }

    private fun buildDisplayPath(
        parentPath: List<String>,
        element: EapplyFormElement,
    ): String {
        return (parentPath + (element.id ?: element.displayName ?: UNKNOWN_ELEMENT)).joinToString(".")
    }

    private fun List<String>.toErrorPath(): String {
        return takeIf { it.isNotEmpty() }?.joinToString(".") ?: ROOT_PATH
    }

    private fun isGroupElement(element: EapplyFormElement): Boolean {
        return element.type.equals(GROUP_TYPE, ignoreCase = true)
    }

    private companion object {
        private const val GROUP_TYPE = "Group"
        private const val ROOT_PATH = "root"
        private const val UNKNOWN_ELEMENT = "<unknown>"
    }
}
