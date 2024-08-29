package com.bilderlings.back.service

import com.bilderlings.back.domain.ExchangeRate
import com.bilderlings.back.repository.ExchangeRatesRepository
import com.bilderlings.back.service.fetcher.ECBRatesFetcher
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class ExchangeRateService(
    private val ecbRatesFetcher: ECBRatesFetcher,
    private val exchangeRatesRepository: ExchangeRatesRepository) {

    private val logger = LoggerFactory.getLogger(ExchangeRateService::class.java)
    private var exchangeRates: Map<String, BigDecimal> = emptyMap()

    @PostConstruct
    fun initializeRates() {
        refreshExchangeRates()
    }

    fun refreshExchangeRates(): Map<String, BigDecimal> {
        logger.info("Refreshing exchange rates...")
        val rates = ecbRatesFetcher.fetchRates()

        storeExchangeRates(rates)
        exchangeRates = rates

        clearRateCache()

        logger.info("Fetched {} exchange rates", rates.size)
        return rates
    }

    @CacheEvict(value = ["exchangeRates"], allEntries = true)
    fun clearRateCache() {
        logger.info("Clearing exchange rates cache")
    }

    @Cacheable("exchangeRates")
    fun getRate(currency: String): BigDecimal {
        logger.info("Fetching rate for currency: {}", currency)
        return exchangeRates[currency] ?: throw IllegalArgumentException("Rate for $currency not found")
    }

    private fun storeExchangeRates(rates: Map<String, BigDecimal>) {
        val exchangeRateEntities = rates.map { (currency, rate) ->
            ExchangeRate(currency = currency, rate = rate)
        }
        exchangeRatesRepository.saveAll(exchangeRateEntities)
        logger.info("Stored {} exchange rates in the repository", exchangeRateEntities.size)
    }
}