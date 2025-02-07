package com.drogueria.inventario.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.drogueria.inventario.security.filter.JwtAuthenticationFilter;
import com.drogueria.inventario.security.filter.JwtValidationFilter;
import com.drogueria.inventario.services.JpaUserDetailsService;
import com.drogueria.inventario.services.PermissionsCacheService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SpringSecurityConfig {

    @Autowired
    private PermissionsCacheService permissionsCacheService;

    @Bean
    UserDetailsService userDetailsService() {
        return new JpaUserDetailsService();
    }

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authz) -> authz
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/test").permitAll()
                // .requestMatchers(HttpMethod.GET,"/api/users").hasAnyAuthority("GET /users")

                // Permisos para usuarios
                .requestMatchers(HttpMethod.GET, "/users").hasAuthority("GET /users")
                .requestMatchers(HttpMethod.GET, "/users/{id}").hasAuthority("GET /users/{id}")
                .requestMatchers(HttpMethod.POST, "/users").hasAuthority("POST /users")
                .requestMatchers(HttpMethod.PUT, "/users/{id}").hasAuthority("PUT /users/{id}")
                .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasAuthority("DELETE /users/{id}")
                .requestMatchers(HttpMethod.POST, "/users/{userId}/roles/{roleId}")
                .hasAuthority("POST /users/{userId}/roles/{roleId}")
                .requestMatchers(HttpMethod.DELETE, "/users/{userId}/roles/{roleId}")
                .hasAuthority("DELETE /users/{userId}/roles/{roleId}")

                // Permisos para Gestión de Permisos
                .requestMatchers(HttpMethod.GET, "/permissions").hasAuthority("GET /permissions")
                .requestMatchers(HttpMethod.GET, "/permissions/{id}").hasAuthority("GET /permissions/{id}")
                .requestMatchers(HttpMethod.POST, "/permissions").hasAuthority("POST /permissions")
                .requestMatchers(HttpMethod.PUT, "/permissions/{id}").hasAuthority("PUT /permissions/{id}")
                .requestMatchers(HttpMethod.DELETE, "/permissions/{id}").hasAuthority("DELETE /permissions/{id}")

                // Permisos para Roles
                .requestMatchers(HttpMethod.GET, "/roles").hasAuthority("GET /roles")
                .requestMatchers(HttpMethod.GET, "/roles/{id}").hasAuthority("GET /roles/{id}")
                .requestMatchers(HttpMethod.POST, "/roles").hasAuthority("POST /roles")
                .requestMatchers(HttpMethod.PUT, "/roles/{id}").hasAuthority("PUT /roles/{id}")
                .requestMatchers(HttpMethod.DELETE, "/roles/{id}").hasAuthority("DELETE /roles/{id}")
                .requestMatchers(HttpMethod.POST, "/roles/{roleId}/permissions/{permissionId}")
                .hasAuthority("POST /roles/{roleId}/permissions/{permissionId}")
                .requestMatchers(HttpMethod.DELETE, "/roles/{roleId}/permissions/{permissionId}")
                .hasAuthority("DELETE /roles/{roleId}/permissions/{permissionId}")

                // Permisos para Categorías
                .requestMatchers(HttpMethod.GET, "/categories").hasAuthority("GET /categories")
                .requestMatchers(HttpMethod.GET, "/categories/{id}").hasAuthority("GET /categories/{id}")
                .requestMatchers(HttpMethod.POST, "/categories").hasAuthority("POST /categories")
                .requestMatchers(HttpMethod.PUT, "/categories/{id}").hasAuthority("PUT /categories/{id}")
                .requestMatchers(HttpMethod.DELETE, "/categories/{id}").hasAuthority("DELETE /categories/{id}")

                // Permisos para Distribuidores
                .requestMatchers(HttpMethod.GET, "/distributors").hasAuthority("GET /distributors")
                .requestMatchers(HttpMethod.GET, "/distributors/{id}").hasAuthority("GET /distributors/{id}")
                .requestMatchers(HttpMethod.POST, "/distributors").hasAuthority("POST /distributors")
                .requestMatchers(HttpMethod.PUT, "/distributors/{id}").hasAuthority("PUT /distributors/{id}")
                .requestMatchers(HttpMethod.DELETE, "/distributors/{id}").hasAuthority("DELETE /distributors/{id}")

                // Permisos para forma de venta
                .requestMatchers(HttpMethod.GET, "/sales-forms").hasAuthority("GET /sales-forms")
                .requestMatchers(HttpMethod.GET, "/sales-forms/{id}").hasAuthority("GET /sales-forms/{id}")
                .requestMatchers(HttpMethod.POST, "/sales-forms").hasAuthority("POST /sales-forms")
                .requestMatchers(HttpMethod.PUT, "/sales-forms/{id}").hasAuthority("PUT /sales-forms/{id}")
                .requestMatchers(HttpMethod.DELETE, "/sales-forms/{id}").hasAuthority("DELETE /sales-forms/{id}")

                // Permisos para Productos
                .requestMatchers(HttpMethod.GET, "/products").hasAuthority("GET /products")
                .requestMatchers(HttpMethod.GET, "/products/{id}").hasAuthority("GET /products/{id}")
                .requestMatchers(HttpMethod.GET, "/products/search").hasAuthority("GET /products/search")
                .requestMatchers(HttpMethod.GET, "/products/export").hasAuthority("GET /products/export")
                .requestMatchers(HttpMethod.GET, "/products/expiration-warnings")
                .hasAuthority("GET /products/expiration-warnings")
                .requestMatchers(HttpMethod.POST, "/products").hasAuthority("POST /products")
                .requestMatchers(HttpMethod.PUT, "/products/{id}").hasAuthority("PUT /products/{id}")
                .requestMatchers(HttpMethod.PUT, "/products/{id}/add-stock")
                .hasAuthority("PUT /products/{id}/add-stock")
                .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasAuthority("DELETE /products/{id}")

                // Permisos para Ventas
                .requestMatchers(HttpMethod.POST, "/sales").hasAuthority("POST /sales")
                .requestMatchers(HttpMethod.GET, "/sales/search").hasAuthority("GET /sales/search")
                .requestMatchers(HttpMethod.POST, "/sales/{saleId}/return-entire")
                .hasAuthority("POST /sales/{saleId}/return-entire")
                .requestMatchers(HttpMethod.POST, "/sales/{saleId}/return").hasAuthority("POST /sales/{saleId}/return")
                .requestMatchers(HttpMethod.GET, "/sales/report").hasAuthority("GET /sales/report")

                .anyRequest().authenticated())
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtValidationFilter(authenticationManager(), permissionsCacheService)) // Agregamos el
                                                                                                      // nuevo filtro
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"message\": \"No autorizado\"}");
                        }))
                .csrf(config -> config.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
