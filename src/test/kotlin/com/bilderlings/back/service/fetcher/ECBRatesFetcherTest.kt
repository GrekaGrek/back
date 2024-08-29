package com.bilderlings.back.service.fetcher

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        coEvery { webClientMock.get() } returns requestHeadersUriSpecMock
        coEvery { requestHeadersUriSpecMock.uri(ecbUriMock) } returns requestHeadersUriSpecMock
        coEvery { requestHeadersUriSpecMock.retrieve() } returns responseSpecMock
        coEvery { responseSpecMock.bodyToMono(String::class.java) } returns Mono.just(
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

        coVerify { webClientMock.get() }
        coVerify { requestHeadersUriSpecMock.uri(ecbUriMock) }
        coVerify { requestHeadersUriSpecMock.retrieve() }
        coVerify { responseSpecMock.bodyToMono(String::class.java) }
    }

    @Test
    fun `should throw RuntimeException when rates fetch fails`() = runBlocking {
        coEvery { webClientMock.get() } returns requestHeadersUriSpecMock
        coEvery { requestHeadersUriSpecMock.uri(ecbUriMock) } returns requestHeadersUriSpecMock
        coEvery { requestHeadersUriSpecMock.retrieve() } returns responseSpecMock
        coEvery { responseSpecMock.bodyToMono(String::class.java) } returns Mono.error(
            RuntimeException("Failed to fetch exchange rates from ECB")
        )
        try {
            ecbRatesFetcher.fetchRates()
        } catch (e: RuntimeException) {
            assertEquals("Failed to fetch exchange rates from ECB", e.message)
        }
        coVerify { webClientMock.get() }
        coVerify { requestHeadersUriSpecMock.uri(ecbUriMock) }
        coVerify { requestHeadersUriSpecMock.retrieve() }
        coVerify { responseSpecMock.bodyToMono(String::class.java) }
    }
}