package com.bilderlings.back.it

import com.bilderlings.back.service.CurrencyConversionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PublicCurrencyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Container
    private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest")

    @Test
    fun `should convert currency successfully`() {
        mockMvc.perform(
            get("/public/conversion/convert")
                .param("amount", BigDecimal.valueOf(100.00).toString())
                .param("fromCurrency", "EUR")
                .param("toCurrency", "USD")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("105.34"))
            .andReturn()
    }

    @Test
    fun `should refresh exchange rates successfully`() {
        mockMvc.perform(
            post("/public/conversion/refresh-rates")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
    }
}