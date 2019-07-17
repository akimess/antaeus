package io.pleo.antaeus.core

import io.pleo.antaeus.models.Currency
import org.junit.jupiter.api.Test

class UtilsTest {
    private val correctNumber = 10.toBigDecimal() * 0.8910273545.toBigDecimal()

    @Test
    fun `will return the correct number`() {
        assert(convertCurrency(Currency.USD, Currency.EUR, 10.toBigDecimal()) == correctNumber)
    }
}


