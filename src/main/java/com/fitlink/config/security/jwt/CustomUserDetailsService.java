package com.fitlink.config.security.jwt;

import com.fitlink.domain.Users;
import com.fitlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("?대떦 ?대찓?쇱쓣 媛吏??좎?媛 議댁옱?섏? ?딆뒿?덈떎: " + username));
        return new CustomUserDetails(user);
    }
}

