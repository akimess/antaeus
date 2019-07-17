## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will pay those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Solution

### Thought process explanation
Haven't written in Kotlin before, but it's very familiar for me, because I have previously written in TypeScript and Go.  
After working out the workflow of the application and what different functions return, I drew up a rough workflow of how the subscription process should work and what additional processes I need.  

**Firstly** I started to work on Invoice Service and DAL to add needed functionality that I will be using later in the Billing Service. Also adding tests to some of those functions I created.  
**Secondly** I programmed the Billing Service logic and added the schedule timer to it, so it will execute each month on the first day.  
**Finally** I noticed that the external service "Payment Provider" can return several exceptions. Some of those exceptions can be dealt with and I decided to implement a solution for it, which I will explain down below. After finishing it, I have done some refactoring and testing to see if everything works as intended.

### Exception Handling  
Two exceptions from Payment Provider have caught my eye: **CurrencyMismatchException and NetworkException**.  
Those two errors can be handled if we make some assumptions about the application and it's workflow.
I have made several assumptions:
1. If **NetworkException** returns, that means the external service is down temporarily and it can be reached later. Because of this, I added a solution that will retry the selected invoice again in an hour. This assumption presumes that this external service will not be down permanent and the invoice will be in "retry hell".
2. If **CurrencyMismatchException** happens, this means that the Invoice has the **amount** in the wrong currency, as the Customer currency is different. This allows us to use an external API to get the rates for those currencies and convert the **amount** to the correct currency, after which we will try to process the invoice again.  

#### Time taken: around 7 hours




## Libraries currently in use
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Klaxon](https://github.com/cbeust/klaxon) - Library to parse JSON in Kotlin
