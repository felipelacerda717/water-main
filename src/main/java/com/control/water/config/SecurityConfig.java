package com.control.water.config;

import com.control.water.services.UserService;
import com.control.water.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Add this import statement
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Add this import statement
import java.util.List; // Add this import statement
import java.util.Arrays; // Add this import statement
@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired
   private UserService userService;

   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http
           .authorizeHttpRequests(authorize -> authorize
               .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
               .anyRequest().authenticated()
           )
           .formLogin(form -> form
               .loginPage("/login")
               .loginProcessingUrl("/login")
               .defaultSuccessUrl("/dashboard", true)
               .failureUrl("/login?error")
               .permitAll()
           )
           .logout(logout -> logout
               .logoutSuccessUrl("/login?logout")
               .permitAll()
           );

       http.csrf(csrf -> csrf.disable());
       http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

       return http.build();
   }

   @Bean 
   public DaoAuthenticationProvider authenticationProvider() {
       DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
       provider.setUserDetailsService(userDetailsService());
       provider.setPasswordEncoder(passwordEncoder());
       return provider;
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
       return NoOpPasswordEncoder.getInstance(); // Não use em produção
   }

   @Bean
   public UserDetailsService userDetailsService() {
       return username -> {
           User user = userService.findByEmail(username);
           if (user == null) {
               throw new UsernameNotFoundException("Usuário não encontrado");
           }
           return org.springframework.security.core.userdetails.User
                   .withUsername(user.getEmail())
                   .password(user.getPassword())
                   .roles("USER")
                   .build();
       };
   }
}