package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.core.exceptions.NetworkException
import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.schedule

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    private val logger = KotlinLogging.logger {}

    //Single Invoice Process
    fun processInvoice(invoice: Invoice): Invoice {

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
                else -> {
                    invoiceStatus = InvoiceStatus.ERROR
                    logger.error(exception) { "Invoice Process Error" }
                }
            }

        }

        return invoice.copy(status = invoiceStatus)
    }

    //Batch Invoices Process
    fun processInvoices(invoices: List<Invoice>): List<Invoice> {
        return invoices.map { processInvoice(it) }.toList()
    }

    //Get all Pending Invoices and Process
    fun processAllInvoices(): List<Invoice> {
        //Get all invoices with status PENDING
        val invoices = invoiceService.fetchByStatus(InvoiceStatus.PENDING)
        return processInvoices(invoices)
    }

}