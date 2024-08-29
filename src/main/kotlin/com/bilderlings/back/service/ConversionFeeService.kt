package com.bilderlings.back.service

import com.bilderlings.back.domain.ConversionFee
import com.bilderlings.back.repository.ConversionFeeRepository
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class ConversionFeeService(private val repository: ConversionFeeRepository) {

    private val logger = LoggerFactory.getLogger(ConversionFeeService::class.java)

    fun getFeeList(): List<ConversionFee> = repository.findAll().also {
        logger.info("Fetched {} conversion fees", it.size)
    }

    fun getFee(fromCurrency: String, toCurrency: String, defaultFee: BigDecimal): BigDecimal {
        return repository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)?.fee ?: defaultFee
    }

    fun addFee(fromCurrency: String, toCurrency: String, fee: BigDecimal): ConversionFee {
        val feeEntity = ConversionFee(null, fromCurrency, toCurrency, fee)
        return repository.save(feeEntity).also {
            logger.info("Successfully added conversion fee with ID {}", it.id)
        }
    }

    fun editFee(id: Long, fromCurrency: String, toCurrency: String, fee: BigDecimal): ConversionFee {
        val feeEntity = repository.findById(id).orElseThrow { ChangeSetPersister.NotFoundException() }
        feeEntity.fromCurrency = fromCurrency
        feeEntity.toCurrency = toCurrency
        feeEntity.fee = fee

        return repository.save(feeEntity).also {
            logger.info("Successfully updated conversion fee with ID {}", it.id)
        }
    }

    fun removeFee(id: Long) {
        repository.deleteById(id)
        logger.info("Successfully removed conversion fee with ID {}", id)
    }
}