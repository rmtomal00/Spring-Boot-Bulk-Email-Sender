package com.orbaic.email.middleware;

import com.orbaic.email.jwt.JwtManager;
import com.orbaic.email.models.auth.User;
import com.orbaic.email.repositories.UserRepository;
import com.orbaic.email.responseModel.JwtResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepo;

    final JwtManager jwtGenerator;

    private final ApplicationContext applicationContext;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        try{
            String authorizationHeader = request.getHeader("Authorization");
            String[] listPublicSetUrl = new String[]{"/api/v1/auth"};
            for (String url : listPublicSetUrl) {
                if (request.getRequestURI().contains(url)) {
                    filterChain.doFilter(request, response);
                    return;
                }

            }

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                System.out.println(request.getRequestURI());
                System.out.println("Authorization header not found");
                new JwtResponse(response, true, "Missing Jwt Token").res();
                return;
            }
            String[] token = authorizationHeader.split(" ");
            String userJwt = token[1].trim();
            if (userJwt.isBlank()) {
                new JwtResponse(response, true, "Invalid Jwt Token").res();
                return;
            }

            Map userTokenExtract = jwtGenerator.verify(userJwt);

            long id = Long.parseLong(userTokenExtract.get("id").toString());
            if (id == 0 ) {
                new JwtResponse(response, true, "Invalid Jwt Token").res();
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepo.findById(id).orElse(null);
                if (user == null) {
                    throw new UsernameNotFoundException("Invalid Jwt Token");
                }
                UserDetails userDetails = applicationContext.getBean(UserDetailsProvider.class).loadUserByUsername(String.valueOf(user.getId()));
                if (jwtGenerator.validateJwt(userJwt, user.getToken())) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else {
                    throw new Exception("You have given an old token");
                }
            }

            filterChain.doFilter(request, response);
        }catch (Exception e){
            System.out.println(e.getMessage());
            new JwtResponse(response, true, e.getMessage()).res();
        }

    }
}
