package com.bilderlings.back.controller

import com.bilderlings.back.configuration.exception.ExceptionControllerAdvice
import com.bilderlings.back.service.CurrencyConversionService
import com.bilderlings.back.service.ExchangeRateService
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal


@ExtendWith(MockKExtension::class)
class PublicCurrencyControllerTest {

    private lateinit var mockMvc: MockMvc
    private val currencyConversionServiceMock: CurrencyConversionService = mockk()
    private val exchangeRateServiceMock: ExchangeRateService = mockk()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            PublicCurrencyController(currencyConversionServiceMock, exchangeRateServiceMock))
            .setControllerAdvice(ExceptionControllerAdvice())
            .build()
    }

    @Test
    fun `should convert currency successfully`() {
        val (amount, fromCurrency, toCurrency) = getCommonTestData()
        val convertedAmount = BigDecimal.valueOf(90.00)

        every { currencyConversionServiceMock.calculateConversion(amount, fromCurrency, toCurrency) } returns convertedAmount

        mockMvc.perform(
            get("/public/conversion/convert")
                .param("amount", amount.toString())
                .param("fromCurrency", fromCurrency)
                .param("toCurrency", toCurrency)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string(convertedAmount.toString()))
            .andReturn()

        verify { currencyConversionServiceMock.calculateConversion(amount, fromCurrency, toCurrency) }
    }

    @Test
    fun `should handle currency conversion failure`() {
        val (amount, fromCurrency, toCurrency) = getCommonTestData()

        every { currencyConversionServiceMock.calculateConversion(amount, fromCurrency, toCurrency) } throws RuntimeException("Conversion service error")

        mockMvc.perform(
            get("/public/conversion/convert")
                .param("amount", amount.toString())
                .param("fromCurrency", fromCurrency)
                .param("toCurrency", toCurrency)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.details").value("Conversion service error"))
            .andReturn()

        verify { currencyConversionServiceMock.calculateConversion(amount, fromCurrency, toCurrency) }
    }

    @Test
    fun `should handle invalid parameters for currency conversion`() {
        mockMvc.perform(
            get("/public/conversion/convert")
                .param("amount", null)
                .param("fromCurrency", "USD")
                .param("toCurrency", "EUR")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value("Required parameter 'amount' is not present."))
            .andReturn()
    }

    @Test
    fun `should refresh exchange rates successfully`() {
        every { exchangeRateServiceMock.refreshExchangeRates() } returns emptyMap()

        mockMvc.perform(
            post("/public/conversion/refresh-rates")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()

        verify { exchangeRateServiceMock.refreshExchangeRates() }
    }

    @Test
    fun `should handle exchange rate refresh failure`() {
        every { exchangeRateServiceMock.refreshExchangeRates() } throws RuntimeException("Refresh service error")

        mockMvc.perform(
            post("/public/conversion/refresh-rates")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.details").value("Refresh service error"))
            .andReturn()

        verify { exchangeRateServiceMock.refreshExchangeRates() }
    }

    private fun getCommonTestData(): Triple<BigDecimal, String, String> {
        val amount = BigDecimal.valueOf(100.00)
        val fromCurrency = "USD"
        val toCurrency = "EUR"
        return Triple(amount, fromCurrency, toCurrency)
    }
}