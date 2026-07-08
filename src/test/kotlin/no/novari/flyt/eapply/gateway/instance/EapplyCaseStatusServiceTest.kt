package no.novari.flyt.eapply.gateway.instance

import no.novari.flyt.gateway.instance.kafka.ArchiveCaseIdRequestService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication

class EapplyCaseStatusServiceTest {
    private val sourceApplicationAuthorizationService = mock(SourceApplicationAuthorizationService::class.java)
    private val archiveCaseIdRequestService = mock(ArchiveCaseIdRequestService::class.java)
    private val authentication = mock(Authentication::class.java)
    private val service =
        EapplyCaseStatusService(
            sourceApplicationAuthorizationService = sourceApplicationAuthorizationService,
            archiveCaseIdRequestService = archiveCaseIdRequestService,
        )

    @Test
    fun `returns archive case id for source application instance`() {
        `when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication)).thenReturn(42L)
        `when`(archiveCaseIdRequestService.getArchiveCaseId(42L, "instance-123")).thenReturn("2026/456")

        val result = service.getCaseStatus(authentication, "instance-123")

        assertEquals("2026/456", result?.archiveCaseId)
        verify(archiveCaseIdRequestService).getArchiveCaseId(42L, "instance-123")
    }

    @Test
    fun `returns null when archive case id is missing`() {
        `when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication)).thenReturn(42L)

        val result = service.getCaseStatus(authentication, "instance-123")

        assertNull(result)
    }
}
