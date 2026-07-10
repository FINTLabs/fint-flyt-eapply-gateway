package no.novari.flyt.eapply.gateway

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

const val OPEN_API_BEARER_AUTH_SCHEME = "bearerAuth"

@OpenAPIDefinition(
    info =
        Info(
            title = "Flyt eApply Gateway API",
            version = "v1",
            description = "External API for receiving eApply metadata and instances in Flyt.",
        ),
)
@SecurityScheme(
    name = OPEN_API_BEARER_AUTH_SCHEME,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
)
@Configuration
class OpenApiConfig
