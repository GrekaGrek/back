package com.bilderlings.back.it

import com.bilderlings.back.domain.ConversionFee
import com.bilderlings.back.model.FeeRequest
import com.bilderlings.back.repository.ConversionFeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFeeControllerTest {

    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var feeRepository: ConversionFeeRepository

    @Container
    private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest")

    @Test
    @Sql(scripts = ["classpath:test-data.sql"])
    fun `should get list of fees successfully`() {
        mockMvc.perform(
            get("/admin/fees")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    [
                    {"id":4,"fromCurrency":"CHF","toCurrency":"PLN","fee":0.05},
                    {"id":2,"fromCurrency":"HUF","toCurrency":"JPY","fee":0.08},
                    {"id":3,"fromCurrency":"ISK","toCurrency":"NOK","fee":0.02}
                    ]
                """.trimIndent()
                )
            )
            .andReturn()
    }

    @Test
    fun `should add fee successfully`() {
        val feeRequest = FeeRequest("BSD", "AED", BigDecimal.valueOf(0.6))

        mockMvc.perform(
            post("/admin/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(
                content().json(
                    """
                    {"id":1,"fromCurrency":"BSD","toCurrency":"AED","fee":0.6}
                """.trimIndent()
                )
            )
            .andReturn()
    }

    @Test
    @Sql(scripts = ["classpath:test-data.sql"])
    fun `should update fee successfully`() {
        val feeRequest = FeeRequest("HUF", "JPY", BigDecimal.valueOf(0.4))
        val updatedFee = ConversionFee(id = 2L, fromCurrency = "HUF", toCurrency = "JPY", fee = BigDecimal.valueOf(0.4))

        mockMvc.perform(
            put("/admin/fees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(updatedFee)))
            .andReturn()
    }

    @Test
    fun `should delete fee successfully`() {
        mockMvc.perform(
            delete("/admin/fees/3")
        )
            .andExpect(status().isNoContent)
            .andReturn()
    }
}