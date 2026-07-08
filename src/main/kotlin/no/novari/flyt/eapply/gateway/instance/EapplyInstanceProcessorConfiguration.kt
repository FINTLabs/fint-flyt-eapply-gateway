package no.novari.flyt.eapply.gateway.instance

import no.novari.flyt.eapply.gateway.instance.model.EapplyInstance
import no.novari.flyt.gateway.instance.InstanceProcessorFactoryService
import no.novari.flyt.gateway.instance.MultipartInstanceProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EapplyInstanceProcessorConfiguration {
    @Bean
    fun eapplyInstanceProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        eapplyInstanceMapper: EapplyInstanceMapper,
    ): MultipartInstanceProcessor<EapplyInstance> {
        return instanceProcessorFactoryService.createMultipartInstanceProcessor(
            sourceApplicationIntegrationIdFunction = { eapplyInstance ->
                requireNotNull(eapplyInstance.metadata).formId.orEmpty()
            },
            sourceApplicationInstanceIdFunction = { eapplyInstance ->
                requireNotNull(eapplyInstance.metadata).instanceId.orEmpty()
            },
            multipartInstanceMapper = eapplyInstanceMapper,
        )
    }
}
