package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val invoiceObj = Invoice(200, 1, Money(200.toBigDecimal(),
            Currency.EUR), InvoiceStatus.PENDING)
    private val paidInvoiceObj = invoiceObj.copy(status = InvoiceStatus.PAID)
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchInvoice(200) } returns invoiceObj
        every { updateInvoice(invoiceObj) } returns paidInvoiceObj
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will change status to PAID`() {
        assert(invoiceService.changeStatus(invoiceObj, InvoiceStatus.PAID) ==
                paidInvoiceObj )
    }


}