package no.novari.flyt.example.gateway.instance.simple

import no.novari.flyt.example.gateway.instance.model.SimpleExample
import no.novari.flyt.gateway.webinstance.model.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimpleMappingServiceTest {
    private val service = SimpleMappingService()

    @Test
    fun `maps simple example fields to instance object`() {
        val input =
            SimpleExample(
                sysId = "sys-1",
                string1 = "hello",
                int1 = 42,
            )

        val result =
            service.map(
                sourceApplicationId = 1L,
                incomingInstance = input,
                persistFile = { _: File ->
                    throw IllegalStateException("persistFile should not be called for simple mapping")
                },
            )

        assertEquals(
            mapOf(
                "sysId" to "sys-1",
                "string1" to "hello",
                "int1" to "42",
            ),
            result.valuePerKey,
        )
        assertTrue(result.objectCollectionPerKey.isEmpty())
    }
}
