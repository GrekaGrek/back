package com.bilderlings.back.service.fetcher

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class ECBRatesFetcher(
    private val webClient: WebClient,
    @Value("\${webclient.ecb-uri}") private val ecbUri: String) {

    private val logger = LoggerFactory.getLogger(ECBRatesFetcher::class.java)
    private val xmlMapper = XmlMapper()

    fun fetchRates(): Map<String, BigDecimal> {
        logger.info("Starting to fetch exchange rates from ECB URI: {}", ecbUri)

        val ratesXml = webClient.get()
            .uri(ecbUri)
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: throw RuntimeException("Failed to fetch exchange rates from ECB")

        logger.info("Successfully fetched rates XML from ECB")
        return extractRates(ratesXml)
    }

    private fun extractRates(xml: String): Map<String, BigDecimal> {
        val result = mutableMapOf<String, BigDecimal>()
        val root = xmlMapper.readTree(xml)
        val cubeNodes = root.path("Cube").path("Cube").path("Cube")

        cubeNodes.forEach { node ->
            val currency = node.get("currency").asText()
            val rate = node.get("rate").asText().toBigDecimal()

            result[currency] = rate
        }
        logger.info("Successfully parsed ECB rates XML and extracted {} currencies", result.size)
        return result
    }
}