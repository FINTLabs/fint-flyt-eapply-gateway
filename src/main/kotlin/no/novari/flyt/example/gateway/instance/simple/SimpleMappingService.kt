package no.novari.flyt.example.gateway.instance.simple

import no.novari.flyt.example.gateway.instance.mapping.putOrEmpty
import no.novari.flyt.example.gateway.instance.model.SimpleExample
import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SimpleMappingService : InstanceMapper<SimpleExample> {
    override fun map(
        @Suppress("UNUSED_PARAMETER") sourceApplicationId: Long,
        incomingInstance: SimpleExample,
        @Suppress("UNUSED_PARAMETER") persistFile: (File) -> UUID,
    ): InstanceObject {
        return with(incomingInstance) {
            val valuePerKey =
                buildMap {
                    putOrEmpty("sysId", sysId)
                    putOrEmpty("string1", string1)
                    putOrEmpty("int1", int1)
                }

            InstanceObject(valuePerKey)
        }
    }
}
