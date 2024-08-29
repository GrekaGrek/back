package com.bilderlings.back.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal


@Entity
@Table(name = "exchange_rates")
data class ExchangeRate(
    @Id
    val currency: String,
    val rate: BigDecimal
)
