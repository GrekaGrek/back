package com.bilderlings.back.configuration.exception

import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionControllerAdvice : ResponseEntityExceptionHandler() {

    private val logger = LoggerFactory.getLogger(ExceptionControllerAdvice::class.java)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult
            .fieldErrors
            .associate { (it.field) to (it.defaultMessage ?: "Invalid value") }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            errors = errors
        )
        logger.error("Validation failed: $errors")

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<Any> {
        logger.error("NotFoundException occurred", ex)

        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "Resource not found"
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Unexpected error occurred", ex)

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "An unexpected error occurred",
            details = ex.localizedMessage
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ErrorResponse(
        val status: HttpStatus,
        val message: String,
        val errors: Map<String, String> = emptyMap(),
        val details: String? = null
    )
}