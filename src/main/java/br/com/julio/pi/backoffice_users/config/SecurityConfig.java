package br.com.julio.pi.backoffice_users.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.julio.pi.backoffice_users.security.DbUserDetailsService;
import br.com.julio.pi.backoffice_users.security.JwtAuthFilter;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final DbUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder; // vem do PasswordConfig

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            DbUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // H2 console
                .requestMatchers("/h2-console/**").permitAll()

                // páginas/estáticos
                .requestMatchers(
                    "/", "/index.html", "/product.html", "/login.html",
                    "/styles.css", "/product.css",
                    "/js/**", "/imagens/**", "/logo.png"
                ).permitAll()

                // auth API
                .requestMatchers("/api/auth/**").permitAll()

                // APIs públicas do site
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()
                .requestMatchers("/api/carrinho/**", "/api/frete/**").permitAll()

                // admin-only
                .requestMatchers(HttpMethod.POST,   "/api/produtos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("ADMINISTRADOR")

                .requestMatchers("/api/clientes/auth/**").permitAll()
                .requestMatchers("/api/clientes/**").hasRole("CLIENTE")

                // o resto
                .anyRequest().permitAll()
            )

            // necessário para H2 em frames
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))

            // autenticação via banco
            .authenticationProvider(daoAuthProvider())

            // filtro JWT
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:3000"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
