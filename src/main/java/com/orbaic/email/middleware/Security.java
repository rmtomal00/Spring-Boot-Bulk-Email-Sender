package com.orbaic.email.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbaic.email.enums.Role;
import com.orbaic.email.exception.ExceptionRequestHandle;
import com.orbaic.email.jwt.JwtManager;
import com.orbaic.email.models.auth.User;
import com.orbaic.email.repositories.UserRepository;
import com.orbaic.email.responseModel.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;
import java.util.Optional;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class Security{


    final JwtFilter filter;

    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtManager generator;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/admin/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(error -> error.authenticationEntryPoint(new ExceptionRequestHandle()))
                .logout(logout ->
                        logout
                                .logoutUrl("/api/v1/auth/logout")
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    String token = request.getHeader("Authorization");
                                    if(token == null){
                                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                                        response.addHeader("content-type", "application/json");
                                        response.addHeader("Access-Control-Allow-Origin", "*");
                                        response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().error("Token Not Send", 400)));
                                        return;
                                    }
                                    String split = token.split(" ")[1];
                                    if(split == null){
                                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                                        response.addHeader("content-type", "application/json");
                                        response.addHeader("Access-Control-Allow-Origin", "*");
                                        response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().error("Token Not Send", 400)));
                                        return;
                                    }
                                    long username = 0;
                                    try {
                                        Map map = generator.verify(split);
                                        username = (int) map.get("id");
                                    }catch (Exception e){
                                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                                        response.addHeader("content-type", "application/json");
                                        response.addHeader("Access-Control-Allow-Origin", "*");
                                        response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().error(e.getMessage(), 400)));
                                        return;

                                    }
                                    Optional<User> user = userRepo.findById(username);

                                    if(user.isEmpty()){
                                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                                        response.addHeader("content-type", "application/json");
                                        response.addHeader("Access-Control-Allow-Origin", "*");
                                        response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().error("User not found. May be deleted by admin", 400)));
                                        return;
                                    }

                                    if(!split.trim().equals(user.get().getToken())){
                                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                                        response.addHeader("content-type", "application/json");
                                        response.addHeader("Access-Control-Allow-Origin", "*");
                                        response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().error("Token is already revoked", 400)));
                                        return;
                                    }

                                    user.get().setToken(null);
                                    userRepo.save(user.get());
                                    response.setStatus(HttpStatus.OK.value());
                                    response.addHeader("content-type", "application/json");
                                    response.addHeader("Access-Control-Allow-Origin", "*");
                                    response.getWriter().write(new ObjectMapper().writeValueAsString(new CustomResponse().successWithoutData("Successfully logout")));
                                    SecurityContextHolder.clearContext();
                                })
                );

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(BCryptPasswordEncoder bCryptPasswordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(new UserDetailsProvider(userRepo));
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
