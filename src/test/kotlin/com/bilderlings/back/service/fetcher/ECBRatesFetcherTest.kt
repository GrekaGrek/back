package com.bilderlings.back.service.fetcher

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal


class ECBRatesFetcherTest {
    private val webClientMock = mockk<WebClient>()
    private val requestHeadersUriSpecMock = mockk<WebClient.RequestHeadersUriSpec<*>>()
    private val responseSpecMock = mockk<WebClient.ResponseSpec>()

    private val ecbUriMock = "https://mocked-ecb-uri.com"
    private val ecbRatesFetcher = ECBRatesFetcher(webClientMock, ecbUriMock)

    @Test
    fun `should fetch and extract rates successfully`() = runBlocking {
        every { webClientMock.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.uri(ecbUriMock) } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.bodyToMono(String::class.java) } returns Mono.just(
            """
            <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <Cube>
                <Cube>
                  <Cube currency="USD" rate="1.12"/>
                  <Cube currency="JPY" rate="161.24"/>
                </Cube>
              </Cube>
            </gesmes:Envelope>
            """.trimIndent()
        )

        val rates = ecbRatesFetcher.fetchRates()
        val expectedRates = mapOf(
            "USD" to BigDecimal.valueOf(1.12),
            "JPY" to BigDecimal.valueOf(161.24)
        )
        assertEquals(expectedRates, rates)

        verify { webClientMock.get() }
        verify { requestHeadersUriSpecMock.uri(ecbUriMock) }
        verify { requestHeadersUriSpecMock.retrieve() }
        verify { responseSpecMock.bodyToMono(String::class.java) }
    }

    @Test
    fun `should throw RuntimeException when rates fetch fails`() = runBlocking {
        every { webClientMock.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.uri(ecbUriMock) } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.bodyToMono(String::class.java) } returns Mono.error(
            RuntimeException("Failed to fetch exchange rates from ECB")
        )
        try {
            ecbRatesFetcher.fetchRates()
        } catch (e: RuntimeException) {
            assertEquals("Failed to fetch exchange rates from ECB", e.message)
        }
        verify { webClientMock.get() }
        verify { requestHeadersUriSpecMock.uri(ecbUriMock) }
        verify { requestHeadersUriSpecMock.retrieve() }
        verify { responseSpecMock.bodyToMono(String::class.java) }
    }
}