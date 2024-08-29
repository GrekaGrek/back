package com.bilderlings.back.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode


@Service
class CurrencyConversionService(
    private val feeService: ConversionFeeService,
    private val exchangeRateService: ExchangeRateService,
    @Value("\${default.fee}") private val defaultFee: BigDecimal) {

    private val logger = LoggerFactory.getLogger(CurrencyConversionService::class.java)

    fun calculateConversion(amount: BigDecimal, fromCurrency: String, toCurrency: String): BigDecimal {
        val fee = feeService.getFee(fromCurrency, toCurrency, defaultFee)
        logger.info("Fetched fee for conversion from {} to {}: {}", fromCurrency, toCurrency, fee)

        val rate = exchangeRateService.getRate(toCurrency)
        logger.info("Fetched exchange rate: {} for {}", rate, toCurrency)

        val convertedAmount = ((amount - amount * fee) * rate).setScale(2, RoundingMode.HALF_UP)
        logger.info("Calculated converted amount: {} {} = {} {}", amount, fromCurrency, convertedAmount, toCurrency)

        return convertedAmount
    }
}