package no.novari.flyt.example.gateway.instance.simple

import no.novari.flyt.example.gateway.instance.model.SimpleExample
import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.gateway.webinstance.InstanceProcessorFactoryService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SimpleProcessorConfiguration {
    @Bean(name = ["simpleProcessor"])
    fun simpleProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        simpleMappingService: SimpleMappingService,
    ): InstanceProcessor<SimpleExample> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            "simpleExample",
            { se -> se.sysId },
            simpleMappingService,
        )
    }
}
