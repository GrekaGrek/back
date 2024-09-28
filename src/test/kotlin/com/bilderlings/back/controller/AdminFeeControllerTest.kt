package com.bilderlings.back.controller

import com.bilderlings.back.configuration.exception.ExceptionControllerAdvice
import com.bilderlings.back.domain.ConversionFee
import com.bilderlings.back.model.FeeRequest
import com.bilderlings.back.service.ConversionFeeService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal


@ExtendWith(MockKExtension::class)
class AdminFeeControllerTest {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()
    private val feeServiceMock: ConversionFeeService = mockk()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(AdminFeeController(feeServiceMock))
            .setControllerAdvice(ExceptionControllerAdvice())
            .build()
    }

    @Test
    fun `should get list of all fees`() {
        val fees = listOf(
            ConversionFee(1, "USD", "EUR", BigDecimal.valueOf(1.12)),
            ConversionFee(2, "GBP", "JPY", BigDecimal.valueOf(0.75))
        )
        every { feeServiceMock.getFeeList() } returns fees

        mockMvc.perform(
            get("/admin/fees")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(fees)))
            .andReturn()

        verify { feeServiceMock.getFeeList() }
    }

    @Test
    fun `should return 500 when fetching fees if service throws exception`() {
        every { feeServiceMock.getFeeList() } throws RuntimeException("Unexpected error")

        mockMvc.perform(
            get("/admin/fees")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.details").value("Unexpected error"))
            .andReturn()

        verify { feeServiceMock.getFeeList() }
    }

    @Test
    fun `should add a fee`() {
        val feeRequest = FeeRequest("USD", "EUR", BigDecimal.valueOf(1.12))
        val newFee = ConversionFee(1, "USD", "EUR", BigDecimal.valueOf(1.12))

        every { feeServiceMock.addFee(feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) } returns newFee

        mockMvc.perform(
            post("/admin/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andReturn()

        verify { feeServiceMock.addFee(feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) }
    }

    @Test
    fun `should return 500 when adding a fee if service throws exception`() {
        val feeRequest = FeeRequest("USD", "EUR", BigDecimal.valueOf(1.12))

        every { feeServiceMock.addFee(feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) } throws RuntimeException("Unexpected error")

        mockMvc.perform(
            post("/admin/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andDo(print())
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.details").value("Unexpected error"))
            .andReturn()

        verify { feeServiceMock.addFee(feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) }
    }

    @Test
    fun `should throw MethodArgumentNotValidException when adding a fee with invalid data`() {
        val invalidFeeRequest = FeeRequest("EURO", "EUR", BigDecimal.valueOf(-1.12))

        mockMvc.perform(
            post("/admin/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidFeeRequest))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.fromCurrency").value("Currency must be exactly 3 characters"))
            .andExpect(jsonPath("$.errors.fee").value("Fee must be a positive value"))
            .andReturn()

        verify(exactly = 0) { feeServiceMock.addFee(any(), any(), any()) }
    }

    @Test
    fun `should edit a fee`() {
        val feeRequest = FeeRequest("USD", "EUR", BigDecimal.valueOf(0.2))
        val updatedFee = ConversionFee(1, "USD", "EUR", BigDecimal.valueOf(0.5))

        every {
            feeServiceMock.editFee(
                1,
                feeRequest.fromCurrency,
                feeRequest.toCurrency,
                feeRequest.fee
            )
        } returns updatedFee

        mockMvc.perform(
            put("/admin/fees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(updatedFee.id))
            .andExpect(jsonPath("$.fromCurrency").value("USD"))
            .andExpect(jsonPath("$.toCurrency").value("EUR"))
            .andExpect(jsonPath("$.fee").value(BigDecimal.valueOf(0.5)))
            .andReturn()

        verify { feeServiceMock.editFee(
            1,
            feeRequest.fromCurrency,
            feeRequest.toCurrency,
            feeRequest.fee) }
    }

    @Test
    fun `should throw NotFoundException when editing a fee with non-existent id`() {
        val feeRequest = FeeRequest("USD", "EUR", BigDecimal.valueOf(1.12))

        every { feeServiceMock.editFee(999, feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) } throws NotFoundException()

        mockMvc.perform(
            put("/admin/fees/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feeRequest))
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Resource not found"))
            .andReturn()

        verify { feeServiceMock.editFee(999, feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee) }
    }

    @Test
    fun `should remove a fee`() {
        every { feeServiceMock.removeFee(any()) } returns Unit

        mockMvc.perform(
            delete("/admin/fees/{id}", 1L)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
            .andReturn()

        verify { feeServiceMock.removeFee(1) }
    }

    @Test
    fun `should throw NotFoundException when removing a fee with non-existent id`() {
        every { feeServiceMock.removeFee(999) } throws NotFoundException()

        mockMvc.perform(
            delete("/admin/fees/{id}", 999L)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Resource not found"))
            .andReturn()

        verify { feeServiceMock.removeFee(999) }
    }
}