package no.novari.flyt.eapply.gateway.instance

import com.fasterxml.jackson.databind.JsonNode
import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.gateway.instance.MultipartInstanceMapper
import no.novari.flyt.gateway.instance.exception.MultipartFileReferenceException
import no.novari.flyt.gateway.instance.model.MultipartFileReference
import no.novari.flyt.gateway.instance.model.instance.InstanceObject
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EapplyInstanceMapper : MultipartInstanceMapper<EapplyInstance> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: EapplyInstance,
        persistFile: (MultipartFileReference) -> UUID,
    ): InstanceObject {
        val valuePerKey = linkedMapOf<String, String>()
        val objectCollectionPerKey = linkedMapOf<String, MutableList<InstanceObject>>()

        incomingInstance.elements.orEmpty().forEach { element ->
            mapValue(
                key = requireNotNull(element.id),
                value = element.value,
                valuePerKey = valuePerKey,
                objectCollectionPerKey = objectCollectionPerKey,
                persistFile = persistFile,
            )
        }

        return InstanceObject(
            valuePerKey = valuePerKey,
            objectCollectionPerKey = objectCollectionPerKey.toInstanceObjectCollections(),
        )
    }

    private fun mapValue(
        key: String,
        value: JsonNode?,
        valuePerKey: MutableMap<String, String>,
        objectCollectionPerKey: MutableMap<String, MutableList<InstanceObject>>,
        persistFile: (MultipartFileReference) -> UUID,
    ) {
        when {
            value == null || value.isNull -> valuePerKey.putValue(key, "")
            value.isArray -> objectCollectionPerKey.addObjects(key, value.map { toInstanceObject(it, persistFile) })
            value.isObject ->
                mapObject(
                    keyPrefix = key,
                    value = value,
                    valuePerKey = valuePerKey,
                    objectCollectionPerKey = objectCollectionPerKey,
                    persistFile = persistFile,
                )

            else -> valuePerKey.putValue(key, value.toInstanceValue())
        }
    }

    private fun toInstanceObject(
        value: JsonNode,
        persistFile: (MultipartFileReference) -> UUID,
    ): InstanceObject {
        val valuePerKey = linkedMapOf<String, String>()
        val objectCollectionPerKey = linkedMapOf<String, MutableList<InstanceObject>>()

        if (value.isObject) {
            mapObject(
                keyPrefix = null,
                value = value,
                valuePerKey = valuePerKey,
                objectCollectionPerKey = objectCollectionPerKey,
                persistFile = persistFile,
            )
        } else {
            valuePerKey.putValue(DEFAULT_SCALAR_COLLECTION_ITEM_KEY, value.toInstanceValue())
        }

        return InstanceObject(
            valuePerKey = valuePerKey,
            objectCollectionPerKey = objectCollectionPerKey.toInstanceObjectCollections(),
        )
    }

    private fun mapObject(
        keyPrefix: String?,
        value: JsonNode,
        valuePerKey: MutableMap<String, String>,
        objectCollectionPerKey: MutableMap<String, MutableList<InstanceObject>>,
        persistFile: (MultipartFileReference) -> UUID,
    ) {
        val fileReference = value.toMultipartFileReferenceOrNull()
        if (fileReference != null) {
            mapObjectFields(
                keyPrefix = keyPrefix,
                value = value,
                valuePerKey = valuePerKey,
                objectCollectionPerKey = objectCollectionPerKey,
                persistFile = persistFile,
                ignoredFields = FILE_REFERENCE_FIELDS,
            )
            valuePerKey.putValue(keyPrefix.toKey(value.fileValueKey()), persistFile(fileReference).toString())
            return
        }

        mapObjectFields(
            keyPrefix = keyPrefix,
            value = value,
            valuePerKey = valuePerKey,
            objectCollectionPerKey = objectCollectionPerKey,
            persistFile = persistFile,
            ignoredFields = emptySet(),
        )
    }

    private fun mapObjectFields(
        keyPrefix: String?,
        value: JsonNode,
        valuePerKey: MutableMap<String, String>,
        objectCollectionPerKey: MutableMap<String, MutableList<InstanceObject>>,
        persistFile: (MultipartFileReference) -> UUID,
        ignoredFields: Set<String>,
    ) {
        value
            .properties()
            .asSequence()
            .filterNot { (fieldName, _) -> fieldName in ignoredFields }
            .forEach { (fieldName, fieldValue) ->
                mapValue(
                    key = keyPrefix.toKey(fieldName),
                    value = fieldValue,
                    valuePerKey = valuePerKey,
                    objectCollectionPerKey = objectCollectionPerKey,
                    persistFile = persistFile,
                )
            }
    }

    private fun JsonNode.toMultipartFileReferenceOrNull(): MultipartFileReference? {
        if (!has(PART_NAME_FIELD)) {
            return null
        }

        return MultipartFileReference(
            partName = requiredTextField(PART_NAME_FIELD),
            fileName = textField(FILE_NAME_FIELD),
            originalFilename = textField(ORIGINAL_FILENAME_FIELD),
            type = (textField(MEDIA_TYPE_FIELD) ?: textField(TYPE_FIELD))?.toMediaType(),
            encoding = textField(ENCODING_FIELD) ?: DEFAULT_ENCODING,
        )
    }

    private fun JsonNode.fileValueKey(): String {
        return textField(FILE_KEY_FIELD) ?: DEFAULT_FILE_VALUE_KEY
    }

    private fun JsonNode.requiredTextField(fieldName: String): String {
        return textField(fieldName)
            ?: throw MultipartFileReferenceException("$fieldName must not be blank")
    }

    private fun JsonNode.textField(fieldName: String): String? {
        return get(fieldName)
            ?.takeUnless { it.isNull }
            ?.asText()
            ?.takeIf(String::isNotBlank)
    }

    private fun String.toMediaType(): MediaType {
        return try {
            MediaType.parseMediaType(this)
        } catch (_: IllegalArgumentException) {
            throw MultipartFileReferenceException("Invalid mediaType '$this'")
        }
    }

    private fun JsonNode.toInstanceValue(): String {
        return when {
            isNull -> ""
            isTextual || isNumber || isBoolean -> asText()
            else -> toString()
        }
    }

    private fun MutableMap<String, String>.putValue(
        key: String,
        value: String,
    ) {
        merge(key, value) { existingValue, newValue -> "$existingValue\n$newValue" }
    }

    private fun MutableMap<String, MutableList<InstanceObject>>.addObjects(
        key: String,
        values: Collection<InstanceObject>,
    ) {
        getOrPut(key) { mutableListOf() }.addAll(values)
    }

    private fun MutableMap<String, MutableList<InstanceObject>>.toInstanceObjectCollections():
        MutableMap<String, Collection<InstanceObject>> {
        return mapValuesTo(linkedMapOf<String, Collection<InstanceObject>>()) { (_, value) -> value.toList() }
    }

    private fun String?.toKey(fieldName: String): String {
        return if (isNullOrBlank()) {
            fieldName
        } else {
            "$this.$fieldName"
        }
    }

    private companion object {
        private const val DEFAULT_ENCODING = "binary"
        private const val DEFAULT_FILE_VALUE_KEY = "file"
        private const val DEFAULT_SCALAR_COLLECTION_ITEM_KEY = "value"
        private const val ENCODING_FIELD = "encoding"
        private const val FILE_KEY_FIELD = "fileKey"
        private const val FILE_NAME_FIELD = "fileName"
        private const val MEDIA_TYPE_FIELD = "mediaType"
        private const val ORIGINAL_FILENAME_FIELD = "originalFilename"
        private const val PART_NAME_FIELD = "partName"
        private const val TYPE_FIELD = "type"

        private val FILE_REFERENCE_FIELDS =
            setOf(
                ENCODING_FIELD,
                FILE_KEY_FIELD,
                ORIGINAL_FILENAME_FIELD,
                PART_NAME_FIELD,
            )
    }
}
