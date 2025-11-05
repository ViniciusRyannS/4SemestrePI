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
    private final PasswordEncoder passwordEncoder;

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
            // mesmo com carrinho usando HttpSession, manter STATELESS evita que o Security crie sessão própria
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // H2
                .requestMatchers("/h2-console/**").permitAll()

                // Páginas públicas e arquivos estáticos
     .requestMatchers(
  "/", "/index.html", "/product.html", "/login.html",
  "/cliente-login.html", "/cliente-perfil.html",
  "/cliente-pedidos.html", "/cliente-pedido.html",
  "/cliente-checkout.html",           // <— novo
  "/styles.css", "/product.css",
  "/js/**", "/imagens/**", "/logo.png"
).permitAll()



                // Auth (backoffice) e Auth do cliente
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/clientes/auth/**").permitAll()

                // APIs abertas (catálogo, carrinho e cálculo de frete)
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()
                .requestMatchers("/api/carrinho/**").permitAll()
                .requestMatchers("/api/frete/**").permitAll()

                // Admin-only – atenção: hasRole("ADMINISTRADOR") espera autoridade "ROLE_ADMINISTRADOR"
                .requestMatchers(HttpMethod.POST,   "/api/produtos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/produtos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("ADMINISTRADOR")

                // Rotas protegidas do domínio de clientes (exceto /auth/** acima)
                .requestMatchers("/api/clientes/**").hasRole("CLIENTE")

                // Qualquer outra rota que não mapeamos explicitamente
                .anyRequest().permitAll()
            )

            // H2 dentro de frame
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))

            // Autenticação por DAO (backoffice)
            .authenticationProvider(daoAuthProvider())

            // Filtro JWT (backoffice + cliente)
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
