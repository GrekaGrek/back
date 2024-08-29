package com.bilderlings.back.repository

import com.bilderlings.back.domain.ExchangeRate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExchangeRatesRepository : JpaRepository<ExchangeRate, String> {
}