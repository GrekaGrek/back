package com.bilderlings.back.service

import com.bilderlings.back.domain.ConversionFee
import com.bilderlings.back.repository.ConversionFeeRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.math.BigDecimal
import java.util.*

class ConversionFeeServiceTest {

    private val repositoryMock = mockk<ConversionFeeRepository>(relaxed = true)
    private val feeService = ConversionFeeService(repositoryMock)

    // Reusable constants
    private val fromCurrency = "USD"
    private val toCurrency = "EUR"
    private val fee = BigDecimal.valueOf(0.02)
    private val anotherFromCurrency = "NOK"
    private val anotherToCurrency = "USD"
    private val anotherFee = BigDecimal.valueOf(0.3)
    private val defaultFee = BigDecimal.valueOf(0.05)

    @Test
    fun `should get fee list`() {
        val feeList = createFeeList()
        every { repositoryMock.findAll() } returns feeList

        val result = feeService.getFeeList()

        assertNotNull(result, "The result must not be null")
        assertEquals(feeList, result)

        verify { repositoryMock.findAll() }
    }

    @Test
    fun `should get fee if exists`() {
        val expectedFee = fee
        every { repositoryMock.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency) } returns
                ConversionFee(1, fromCurrency, toCurrency, fee)

        val result = feeService.getFee(fromCurrency, toCurrency, BigDecimal.ZERO)

        assertEquals(expectedFee, result)
        verify { repositoryMock.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency) }
    }

    @Test
    fun `should return default fee if fee does not exist`() {
        val expectedFee = defaultFee
        every { repositoryMock.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency) } returns null

        val result = feeService.getFee(fromCurrency, toCurrency, defaultFee)

        assertEquals(expectedFee, result)

        verify { repositoryMock.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency) }
    }

    @Test
    fun `should add fee`() {
        val feeEntity = ConversionFee(null, fromCurrency, toCurrency, fee)
        val savedFee = feeEntity.copy(id = 1)
        every { repositoryMock.save(feeEntity) } returns savedFee

        val result = feeService.addFee(fromCurrency, toCurrency, fee)

        assertNotNull(result, "The result should not be null")
        assertEquals(savedFee, result)

        verify { repositoryMock.save(feeEntity) }
    }

    @Test
    fun `should edit fee`() {
        val existingFee = ConversionFee(1L, fromCurrency, toCurrency, fee)
        val updatedFee = existingFee.copy(fromCurrency = fromCurrency, toCurrency = toCurrency, fee = anotherFee)

        every { repositoryMock.findById(1L) } returns Optional.of(existingFee)
        every { repositoryMock.save(updatedFee) } returns updatedFee

        val result = feeService.editFee(1L, fromCurrency, toCurrency, anotherFee)

        assertEquals(updatedFee, result)
        coVerify { repositoryMock.findById(1L) }
        coVerify { repositoryMock.save(updatedFee) }
    }

    @Test
    fun `should throw exception when editing non-existing fee`() {
        every { repositoryMock.findById(45L) } returns Optional.empty()

        assertThrows<NotFoundException> {
            runBlocking {
                feeService.editFee(45L, fromCurrency, toCurrency, fee)
            }
        }
        coVerify { repositoryMock.findById(45L) }
    }

    @Test
    fun `should remove fee`() {
        every { repositoryMock.deleteById(1L) } just Runs

        feeService.removeFee(1L)

        coVerify { repositoryMock.deleteById(1L) }
    }

    private fun createFee(fromCurrency: String, toCurrency: String, fee: BigDecimal): ConversionFee {
        return ConversionFee(null, fromCurrency, toCurrency, fee)
    }

    private fun createFeeList(): List<ConversionFee> {
        return listOf(
            createFee(fromCurrency, toCurrency, fee),
            createFee(anotherFromCurrency, anotherToCurrency, anotherFee)
        )
    }
}