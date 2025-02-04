package br.com.ambev.orderapi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order API")
                        .version("1.0.0")
                        .description("API para gerenciamento de pedidos.")
                        .contact(new Contact()
                                .name("Suporte OrderApp")
                                .email("suporte@ambev.com.br")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação Completa")
                        .url("https://seusite.com/docs"));
    }
}
