package com.bilderlings.back.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class FeeRequest(
    @field:NotBlank(message = "From currency must not be blank")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val fromCurrency: String,

    @field:NotBlank(message = "To currency must not be blank")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val toCurrency: String,

    @field:Min(value = 0, message = "Fee must be a positive value")
    val fee: BigDecimal
)