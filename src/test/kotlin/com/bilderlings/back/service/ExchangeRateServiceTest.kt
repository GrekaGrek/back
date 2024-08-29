package com.bilderlings.back.service

import com.bilderlings.back.domain.ExchangeRate
import com.bilderlings.back.repository.ExchangeRatesRepository
import com.bilderlings.back.service.fetcher.ECBRatesFetcher
import io.mockk.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import java.math.BigDecimal
import kotlin.test.assertFailsWith

class ExchangeRateServiceTest {
    private val ecbRatesFetcherMock = mockk<ECBRatesFetcher>()
    private val exchangeRatesRepositoryMock = mockk<ExchangeRatesRepository>()
    private val cacheManagerMock = mockk<CacheManager>()
    private val cacheMock = mockk<Cache>(relaxed = true)

    private val exchangeRateService = ExchangeRateService(ecbRatesFetcherMock, exchangeRatesRepositoryMock)

    @BeforeEach
    fun setUp() {
        clearMocks(ecbRatesFetcherMock, exchangeRatesRepositoryMock, cacheManagerMock, cacheMock)
        every { exchangeRatesRepositoryMock.saveAll(any<List<ExchangeRate>>()) } returns emptyList()
        every { cacheManagerMock.getCache("exchangeRates") } returns cacheMock
    }

    @Test
    fun `should fetch rate from cache`() {
        val rates = getSampleRates()

        coEvery { ecbRatesFetcherMock.fetchRates() } returns rates
        exchangeRateService.refreshExchangeRates()

        val usdRate = exchangeRateService.getRate("USD")
        val eurRate = exchangeRateService.getRate("GBP")

        assertEquals(BigDecimal.valueOf(1.12), usdRate)
        assertEquals(BigDecimal.valueOf(0.85), eurRate)
    }

    @Test
    fun `should throw IllegalArgumentException for non-existent currency rate`() {
        val rates = getSampleRates()

        coEvery { ecbRatesFetcherMock.fetchRates() } returns rates
        every { exchangeRatesRepositoryMock.saveAll(any<List<ExchangeRate>>()) } returns emptyList()

        exchangeRateService.refreshExchangeRates()

        assertFailsWith<IllegalArgumentException> {
            exchangeRateService.getRate("JPY")
        }
        coVerify { ecbRatesFetcherMock.fetchRates() }
    }

    @Test
    fun `should throw RuntimeException when fetch fails`() {
        coEvery { ecbRatesFetcherMock.fetchRates() } throws RuntimeException("Failed to fetch rates")

        assertFailsWith<RuntimeException> {
            exchangeRateService.refreshExchangeRates()
        }
        coVerify { ecbRatesFetcherMock.fetchRates() }
    }

    private fun getSampleRates(): Map<String, BigDecimal> {
        return mapOf(
            "USD" to BigDecimal.valueOf(1.12),
            "GBP" to BigDecimal.valueOf(0.85)
        )
    }
}