package com.bilderlings.back.it

import com.bilderlings.back.model.ConversionRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PublicCurrencyControllerIntegrationTest {

    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Container
    private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest")

    @Test
    fun `should convert currency successfully`() {
        val conversion = ConversionRequest("EUR", "USD", BigDecimal.valueOf(100.00))

        mockMvc.perform(
            post("/public/conversion/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversion))

        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("104.65"))
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