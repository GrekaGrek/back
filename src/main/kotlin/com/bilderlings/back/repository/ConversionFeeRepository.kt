package com.bilderlings.back.repository

import com.bilderlings.back.domain.ConversionFee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConversionFeeRepository : JpaRepository<ConversionFee, Long> {
    fun findByFromCurrencyAndToCurrency(fromCurrency: String, toCurrency: String): ConversionFee?
}