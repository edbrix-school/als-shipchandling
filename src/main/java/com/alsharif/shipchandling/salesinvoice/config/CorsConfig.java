package com.alsharif.shipchandling.salesinvoice.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
import java.util.Arrays;
import java.util.List;
 
@Configuration
public class CorsConfig implements WebMvcConfigurer {
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Allow all origins - change to specific origins in production
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false) // Set to true if you need credentials (requires specific origins)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
 
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
       
        // Allow all origins - change to specific origins in production
        // Note: Use setAllowedOriginPatterns for wildcard support in Spring Boot 3.x
        config.setAllowedOriginPatterns(List.of("*"));
       
        // Allow all methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
       
        // Allow all headers
        config.setAllowedHeaders(List.of("*"));
       
        // Expose all headers
        config.setExposedHeaders(List.of("*"));
       
        // Allow credentials (set to false if using wildcard origin)
        config.setAllowCredentials(false);
       
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
       
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}