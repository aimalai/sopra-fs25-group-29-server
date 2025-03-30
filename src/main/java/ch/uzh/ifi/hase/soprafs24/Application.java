package ch.uzh.ifi.hase.soprafs24;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String helloWorld() {
        return "The application is running.";
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Adjust CORS settings for Vercel connection and local use
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://sopra-fs25-group-29-client-baxxm6bny-malais-projects-de8e09f0.vercel.app") // Updated to correct Vercel URL.
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Added OPTIONS for preflight requests
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}