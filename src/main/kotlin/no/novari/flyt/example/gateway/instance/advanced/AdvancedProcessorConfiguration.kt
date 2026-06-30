package no.novari.flyt.example.gateway.instance.advanced

import no.novari.flyt.example.gateway.instance.model.AdvancedExample
import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.gateway.webinstance.InstanceProcessorFactoryService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdvancedProcessorConfiguration {
    @Bean(name = ["advancedProcessor"])
    fun advancedProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        advancedMappingService: AdvancedMappingService,
    ): InstanceProcessor<AdvancedExample> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            "advancedExample",
            { ae -> ae.sysId },
            advancedMappingService,
        )
    }
}
