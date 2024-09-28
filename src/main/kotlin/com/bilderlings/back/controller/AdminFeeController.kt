package com.bilderlings.back.controller

import com.bilderlings.back.domain.ConversionFee
import com.bilderlings.back.model.FeeRequest
import com.bilderlings.back.service.ConversionFeeService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/admin/fees")
class AdminFeeController(private val feeService: ConversionFeeService) {

    private val logger = LoggerFactory.getLogger(AdminFeeController::class.java)

    @GetMapping
    fun listOfFees(): ResponseEntity<List<ConversionFee>> {
        logger.info("Returning list of fees: {}", feeService.getFeeList())
        return ResponseEntity(feeService.getFeeList(), HttpStatus.OK)
    }

    @PostMapping
    fun addFee(@RequestBody @Valid feeRequest: FeeRequest): ResponseEntity<ConversionFee> {
        val addedFee = feeService.addFee(feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee)
        logger.info("Successfully added new fee: {}", addedFee)
        return ResponseEntity(addedFee, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun editFee(@PathVariable id: Long, @RequestBody feeRequest: FeeRequest): ResponseEntity<ConversionFee> {
        val updatedFee = feeService.editFee(id, feeRequest.fromCurrency, feeRequest.toCurrency, feeRequest.fee)
        logger.info("Successfully updated fee with ID {}: {}", id, updatedFee)
        return ResponseEntity(updatedFee, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun removeFee(@PathVariable id: Long): ResponseEntity<Void> {
        feeService.removeFee(id)
        logger.info("Successfully deleted fee with ID {}", id)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}