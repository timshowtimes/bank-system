package contracts

import org.springframework.cloud.contract.spec.Contract


Contract.make {
    description "Should withdraw money from account"

    request {
        method POST()
        url("/api/v1/accounts/1/reduce-balance") {
            queryParameters {
                parameter("amount", "500")
            }
        }
    }

    response {
        status OK()
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
                balance: 500.00
        )
    }
}
