package org.ecommerce.config

import org.springframework.context.annotation.Bean

@Bean
fun shipBobWebClient(): WebClient = WebClient.builder()
    .baseUrl("https://api.shipbob.com") // or sandbox-api.shipbob.com
    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${yourToken}")
    .build()