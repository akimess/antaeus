package io.pleo.antaeus.core

import com.beust.klaxon.JsonObject
import io.pleo.antaeus.models.Currency
import java.math.BigDecimal
import java.net.URL
import com.beust.klaxon.Parser

internal fun convertCurrency(from: Currency, to: Currency, amount: BigDecimal): BigDecimal {
    //Get the latest currency exchange rate
    val response = URL("https://api.exchangeratesapi.io/latest?base=${from.name}&symbols=${to.name}")
            .openStream()
            .bufferedReader()
            .use { it.readText() }
    //val rate = (parse(response) as JsonObject).lookup<BigDecimal?>("rates.$from")
    val parser: Parser = Parser.default()
    val stringBuilder: StringBuilder = StringBuilder(response)
    val json: JsonObject = parser.parse(stringBuilder) as JsonObject
    val rate = json.obj("rates")!!.double("EUR")!!.toBigDecimal()
    return rate * amount
}
