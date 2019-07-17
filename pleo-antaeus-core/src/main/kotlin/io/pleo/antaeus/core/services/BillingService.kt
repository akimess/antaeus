package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.convertCurrency
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.models.Money
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
    private val logger = KotlinLogging.logger {}

    fun start(): TimerTask {
        return Timer("processAllInvoices", false)
                .scheduleAtFixedRate(
                        //Start after 1 hour
                        delay = 36000,
                        //Repeat after 24 hours
                        period = 36000 * 24
                ) {
                    //Start the process if it's the first day of the month
                    if (LocalDateTime.now().dayOfMonth == 1){
                        processAllInvoices()
                    }
                }
    }

    //Single Invoice Process
    private fun processInvoice(invoice: Invoice): Invoice {

        val processStatus: Boolean
        var invoiceStatus: InvoiceStatus

        try {

            processStatus = paymentProvider.charge(invoice)
            invoiceStatus = if (processStatus) InvoiceStatus.PAID else InvoiceStatus.PENDING

        } catch (exception: Exception){
            when(exception){
                // Network Error = Try again in an hour
                is NetworkException -> {
                    invoiceStatus = InvoiceStatus.PENDING
                    logger.error(exception) { "Network Error. Will try again" }

                    //Schedule to repeat the function in an hour
                    Timer("processInvoice", false).schedule(3600000){
                        processInvoice(invoice)
                    }
                }
                //Invalid Currency = Convert Currency
                is CurrencyMismatchException -> {
                    logger.error(exception) { "Invalid Currency in Invoice" }
                    val customer = customerService.fetch(invoice.customerId)
                    //Convert invoice currency to customer currency
                    val newAmount = convertCurrency(invoice.amount.currency, customer.currency, invoice.amount.value)
                    val newInvoice = invoice.copy(amount = Money(newAmount, customer.currency))
                    //Charge again with new currency and amount
                    return processInvoice(newInvoice)
                }
                else -> {
                    invoiceStatus = InvoiceStatus.ERROR
                    logger.error(exception) { "Invoice Process Error" }
                }
            }

        }

        return invoiceService.changeStatus(invoice, invoiceStatus)
    }

    //Batch Invoices Process
    private fun processInvoices(invoices: List<Invoice>): List<Invoice> {
        return invoices.map { processInvoice(it) }.toList()
    }

    //Get all Pending Invoices and Process
    private fun processAllInvoices(): List<Invoice> {
        //Get all invoices with status PENDING
        val invoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)
        return processInvoices(invoices)
    }

}