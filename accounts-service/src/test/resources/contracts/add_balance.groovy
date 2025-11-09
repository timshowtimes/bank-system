package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Deposit money to account by ID"

    request {
        method POST()
        urlPath("/api/v1/accounts/1/add-balance") {
            queryParameters {
                parameter("amount", equalTo("500"))
            }
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body(
                id: 1,
                login: "tim",
                firstName: "Timur",
                lastName: "Sultanov",
                email: "timur@example.com",
                birthDate: "1999-08-13",
                balance: 1500.00
        )
    }
}