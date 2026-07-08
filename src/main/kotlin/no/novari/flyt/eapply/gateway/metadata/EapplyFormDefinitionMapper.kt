package no.novari.flyt.eapply.gateway.metadata

import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormDefinition
import no.novari.flyt.eapply.gateway.metadata.model.EapplyFormElement
import no.novari.flyt.gateway.metadata.IntegrationMetadataMapper
import no.novari.flyt.gateway.metadata.model.InstanceMetadataCategory
import no.novari.flyt.gateway.metadata.model.InstanceMetadataContent
import no.novari.flyt.gateway.metadata.model.InstanceObjectCollectionMetadata
import no.novari.flyt.gateway.metadata.model.InstanceValueMetadata
import no.novari.flyt.gateway.metadata.model.IntegrationMetadata
import org.springframework.stereotype.Service

@Service
class EapplyFormDefinitionMapper : IntegrationMetadataMapper<EapplyFormDefinition> {
    override fun toIntegrationMetadata(
        sourceApplicationId: Long,
        incomingMetadata: EapplyFormDefinition,
    ): IntegrationMetadata {
        val metadata = requireNotNull(incomingMetadata.metadata)

        return IntegrationMetadata(
            sourceApplicationId = sourceApplicationId,
            sourceApplicationIntegrationId = requireNotNull(metadata.formId),
            sourceApplicationIntegrationUri = null,
            integrationDisplayName = requireNotNull(metadata.formDisplayName),
            version = metadata.version,
            instanceMetadata = toMetadataContent(incomingMetadata.elements.orEmpty()),
        )
    }

    private fun toMetadataContent(
        elements: List<EapplyFormElement>,
        parentPath: List<String> = emptyList(),
    ): InstanceMetadataContent {
        return InstanceMetadataContent(
            instanceValueMetadata =
                elements
                    .filterNot(::isGroupElement)
                    .map { toInstanceValueMetadata(it, parentPath) },
            instanceObjectCollectionMetadata =
                elements
                    .filter(::isMultipleGroupElement)
                    .map { toInstanceObjectCollectionMetadata(it, parentPath) },
            categories =
                elements
                    .filter(::isSingleGroupElement)
                    .map { toMetadataCategory(it, parentPath) },
        )
    }

    private fun toMetadataCategory(
        element: EapplyFormElement,
        parentPath: List<String>,
    ): InstanceMetadataCategory {
        val elementId = requireNotNull(element.id)
        return InstanceMetadataCategory(
            displayName = requireNotNull(element.displayName),
            content = toMetadataContent(element.elements.orEmpty(), parentPath + elementId),
        )
    }

    private fun toInstanceObjectCollectionMetadata(
        element: EapplyFormElement,
        parentPath: List<String>,
    ): InstanceObjectCollectionMetadata {
        val elementId = requireNotNull(element.id)
        return InstanceObjectCollectionMetadata(
            displayName = requireNotNull(element.displayName),
            objectMetadata = toMetadataContent(element.elements.orEmpty()),
            key = buildKey(parentPath, elementId),
        )
    }

    private fun toInstanceValueMetadata(
        element: EapplyFormElement,
        parentPath: List<String>,
    ): InstanceValueMetadata {
        val elementId = requireNotNull(element.id)
        return InstanceValueMetadata(
            displayName = requireNotNull(element.displayName),
            type = toInstanceValueType(element),
            key = buildKey(parentPath, elementId),
        )
    }

    private fun toInstanceValueType(element: EapplyFormElement): InstanceValueMetadata.Type {
        return when (element.type?.lowercase()) {
            "file" -> InstanceValueMetadata.Type.FILE
            "bool", "boolean" -> InstanceValueMetadata.Type.BOOLEAN
            else -> InstanceValueMetadata.Type.STRING
        }
    }

    private fun isMultipleGroupElement(element: EapplyFormElement): Boolean {
        return isGroupElement(element) && element.multiple
    }

    private fun isSingleGroupElement(element: EapplyFormElement): Boolean {
        return isGroupElement(element) && !element.multiple
    }

    private fun isGroupElement(element: EapplyFormElement): Boolean {
        return element.type.equals(GROUP_TYPE, ignoreCase = true)
    }

    private fun buildKey(
        parentPath: List<String>,
        elementId: String,
    ): String {
        return (parentPath + elementId).joinToString(".")
    }

    private companion object {
        private const val GROUP_TYPE = "Group"
    }
}
