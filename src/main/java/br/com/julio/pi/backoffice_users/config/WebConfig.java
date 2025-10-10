// src/main/java/br/com/julio/pi/backoffice_users/config/WebConfig.java
package br.com.julio.pi.backoffice_users.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Tudo que começar com /imagens/** será servido a partir da pasta ./imagens/ (na raiz do projeto)
        registry.addResourceHandler("/imagens/**")
                .addResourceLocations("file:./imagens/");

        // (Opcional) se você quiser aceitar /uploads/** também
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:./uploads/");
    }
}

