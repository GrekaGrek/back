package com.bilderlings.back.controller

import com.bilderlings.back.service.CurrencyConversionService
import com.bilderlings.back.service.ExchangeRateService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("/public/conversion")
class PublicCurrencyController(
        private val currencyConversionService: CurrencyConversionService,
        private val exchangeRateService: ExchangeRateService) {

    private val logger = LoggerFactory.getLogger(PublicCurrencyController::class.java)

    @GetMapping("/convert")
    fun convert(@RequestParam amount: BigDecimal,
                @RequestParam fromCurrency: String,
                @RequestParam toCurrency: String): BigDecimal {
        logger.info("Received request to convert $amount from $fromCurrency to $toCurrency")
        return currencyConversionService.calculateConversion(amount, fromCurrency, toCurrency)
    }

    @PostMapping("/refresh-rates")
    fun refreshRates() {
        logger.info("Received request to refresh exchange rates")
        exchangeRateService.refreshExchangeRates()
        logger.info("Exchange rates refreshed successfully")
    }
}