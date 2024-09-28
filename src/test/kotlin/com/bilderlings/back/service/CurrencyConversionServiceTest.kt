package com.bilderlings.back.service

import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

class CurrencyConversionServiceTest {

    private val feeServiceMock = mockk<ConversionFeeService>()
    private val exchangeRateServiceMock = mockk<ExchangeRateService>()
    private val defaultFee = BigDecimal("0.05")
    private val currencyConversionService =
        CurrencyConversionService(feeServiceMock, exchangeRateServiceMock, defaultFee)

    // Reusable constants
    private val fromCurrency = "GBP"
    private val toCurrency = "EUR"
    private val fee = BigDecimal.valueOf(0.02)
    private val rate = BigDecimal.valueOf(1.12)

    private fun setupMocks(amount: BigDecimal, fromCurrency: String, toCurrency: String, fee: BigDecimal, rate: BigDecimal) {
        every { feeServiceMock.getFee(fromCurrency, toCurrency, defaultFee) } returns fee
        every { exchangeRateServiceMock.getRate(toCurrency) } returns rate
    }

    @Test
    fun `should calculate conversion with fee and rate`() = runBlocking {
        val amount = BigDecimal.valueOf(100.00)
        val expected = BigDecimal.valueOf(109.76)

        setupMocks(amount, fromCurrency, toCurrency, fee, rate)

        val result = currencyConversionService.calculateConversion(amount, fromCurrency, toCurrency)

        assertEquals(expected, result, "The conversion result should match the expected value")

        verify {
            feeServiceMock.getFee(fromCurrency, toCurrency, defaultFee)
            exchangeRateServiceMock.getRate(toCurrency)
        }
    }

    @Test
    fun `should use default fee if no specific fee found`() = runBlocking {
        val amount = BigDecimal.valueOf(99.00)
        val expected = BigDecimal.valueOf(105.34)

        setupMocks(amount, fromCurrency, toCurrency, defaultFee, rate)

        val result = currencyConversionService.calculateConversion(amount, fromCurrency, toCurrency)

        assertEquals(expected, result)

        verify {
            feeServiceMock.getFee(fromCurrency, toCurrency, defaultFee)
            exchangeRateServiceMock.getRate(toCurrency)
        }
    }

    @Test
    fun `should handle zero amount`() = runBlocking {
        val amount = BigDecimal.ZERO

        setupMocks(amount, fromCurrency, toCurrency, fee, rate)

        val result = currencyConversionService.calculateConversion(amount, fromCurrency, toCurrency)

        assertEquals(BigDecimal.ZERO.setScale(2), result)

        verify {
            feeServiceMock.getFee(fromCurrency, toCurrency, defaultFee)
            exchangeRateServiceMock.getRate(toCurrency)
        }
    }

    @Test
    fun `should handle rounding mode`() = runBlocking {
        val amount = BigDecimal.valueOf(123.45623)
        val expected = BigDecimal.valueOf(135.51)

        setupMocks(amount, fromCurrency, toCurrency, fee, rate)

        val result = currencyConversionService.calculateConversion(amount, fromCurrency, toCurrency)

        assertEquals(expected, result)

        verify {
            feeServiceMock.getFee(fromCurrency, toCurrency, defaultFee)
            exchangeRateServiceMock.getRate(toCurrency)
        }
    }
}