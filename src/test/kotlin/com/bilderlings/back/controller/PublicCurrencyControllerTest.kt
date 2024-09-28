package com.bilderlings.back.controller

import com.bilderlings.back.configuration.exception.ExceptionControllerAdvice
import com.bilderlings.back.model.ConversionRequest
import com.bilderlings.back.service.CurrencyConversionService
import com.bilderlings.back.service.ExchangeRateService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal


@ExtendWith(MockKExtension::class)
class PublicCurrencyControllerTest {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()
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
        val conversionRequest = getCommonTestData()
        val convertedAmount = BigDecimal.valueOf(90.00)

        every { currencyConversionServiceMock.calculateConversion(
            conversionRequest.amount, conversionRequest.fromCurrency, conversionRequest.toCurrency) } returns convertedAmount

        mockMvc.perform(
            post("/public/conversion/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversionRequest))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string(convertedAmount.toString()))
            .andReturn()

        verify { currencyConversionServiceMock.calculateConversion(conversionRequest.amount,
            conversionRequest.fromCurrency, conversionRequest.toCurrency) }
    }

    @Test
    fun `should handle currency conversion failure`() {
        val conversionRequest = getCommonTestData()

        every { currencyConversionServiceMock.calculateConversion(
            conversionRequest.amount, conversionRequest.fromCurrency, conversionRequest.toCurrency) } throws RuntimeException("Conversion service error")

        mockMvc.perform(
            post("/public/conversion/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversionRequest))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.details").value("Conversion service error"))
            .andReturn()

        verify { currencyConversionServiceMock.calculateConversion(conversionRequest.amount,
            conversionRequest.fromCurrency, conversionRequest.toCurrency) }
    }

    @Test
    fun `should handle invalid parameters for currency conversion`() {
        mockMvc.perform(
            post("/public/conversion/convert")
                .param("amount", null)
                .param("fromCurrency", "USD")
                .param("toCurrency", "EUR")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value("Failed to read request"))
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

    private fun getCommonTestData(): ConversionRequest {
        val amount = BigDecimal.valueOf(100.00)
        val fromCurrency = "USD"
        val toCurrency = "EUR"
        return ConversionRequest(fromCurrency, toCurrency, amount)
    }
}