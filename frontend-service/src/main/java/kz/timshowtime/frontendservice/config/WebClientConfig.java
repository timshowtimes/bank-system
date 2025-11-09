//package kz.timshowtime.frontendservice.config;
//
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//public class WebClientConfig {
//
//    @Bean
//    @LoadBalanced
//    public WebClient webClient(OAuth2AuthorizedClientManager manager) {
//        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
//                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
//        oauth2.setDefaultOAuth2AuthorizedClient(true);
//
//        return WebClient.builder()
//                .apply(oauth2.oauth2Configuration())
//                .build();
//    }
//}
//
