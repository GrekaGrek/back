package com.bilderlings.back.controller

import com.bilderlings.back.model.ConversionRequest
import com.bilderlings.back.service.CurrencyConversionService
import com.bilderlings.back.service.ExchangeRateService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("/public/conversion")
class PublicCurrencyController(
    private val currencyConversionService: CurrencyConversionService,
    private val exchangeRateService: ExchangeRateService) {

    private val logger = LoggerFactory.getLogger(PublicCurrencyController::class.java)

    @PostMapping("/convert")
    fun convert(@RequestBody @Valid conversion: ConversionRequest): ResponseEntity<BigDecimal> {
        logger.info(
            "Received request to convert ${conversion.amount} from ${conversion.fromCurrency} to ${conversion.toCurrency}"
        )
        return ResponseEntity.ok(
            currencyConversionService
                .calculateConversion(conversion.amount, conversion.fromCurrency, conversion.toCurrency)
        )
    }

    @PostMapping("/refresh-rates")
    fun refreshRates() : ResponseEntity<String> {
        logger.info("Received request to refresh exchange rates")
        exchangeRateService.refreshExchangeRates()
        return ResponseEntity.ok("Exchange rates refreshed successfully")
    }
}