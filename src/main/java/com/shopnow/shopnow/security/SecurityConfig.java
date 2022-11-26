package com.shopnow.shopnow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JWTFilter filter;
    @Autowired
    private MyUserDetailsService uds;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .cors()
                .and()
                .authorizeHttpRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/administradores/**").hasRole("ADM")
                .antMatchers(HttpMethod.POST, "/api/categorias").hasRole("ADM")
                .antMatchers("/api/compradores/**").hasAnyRole("COMPRADOR", "VENDEDOR")
                .antMatchers("/api/compras/enviadas/{id}", "/api/compras/calificaciones/{id}",
                        "/api/compras/iniciarChat", "/api/compras/chat/{idcompra}", "/api/compras/chats/{idChat}/mensajes").hasAnyRole("COMPRADOR", "VENDEDOR")
                .antMatchers("/api/compras/{idCompra}").hasRole("ADM")
                .antMatchers(HttpMethod.POST, "/api/productos").hasRole("VENDEDOR")
                .antMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADM")
                .antMatchers("/api/usuarios/{uuid}/infoUsuario").hasAnyRole("ADM", "COMPRADOR", "VENDEDOR")
                .antMatchers("/api/usuarios/**").hasAnyRole("COMPRADOR", "VENDEDOR")
                .antMatchers("/api/vendedores/**").hasRole("VENDEDOR")
                .and()
                .userDetailsService(uds)
                .exceptionHandling()
                .authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                )
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
