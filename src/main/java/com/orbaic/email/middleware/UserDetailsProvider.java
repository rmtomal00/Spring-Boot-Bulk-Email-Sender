package com.orbaic.email.middleware;

import com.orbaic.email.models.auth.User;
import com.orbaic.email.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsProvider implements UserDetailsService {
    final UserRepository userRepository;
    public User getUser(){
        if (SecurityContextHolder.getContext().getAuthentication() == null ||  !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userDetails = userRepository.findFirstById(Integer.parseInt(username));
        if (userDetails == null) {
            throw new UsernameNotFoundException(String.format("User %s not found", username));
        }
        if (!userDetails.isEnabled()) {
            throw new UsernameNotFoundException(String.format("User %s is disabled", username));
        }
        if (!userDetails.getIsEmailVerified()){
            throw new UsernameNotFoundException(String.format("User email %s is not verified", username));
        }
        return userDetails;
    }
}
