package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomUserDetails(user);
    }

    // UserDetails를 확장한 내부 클래스
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final User user;

        public CustomUserDetails(User user) {
            super(user.getUsername(), user.getPassword(), new ArrayList<>()); // 권한은 나중에 추가
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        // 필요한 경우 추가적인 UserDetails 메소드를 오버라이드할 수 있습니다.
        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return new ArrayList<>(); // 현재는 권한 없음
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
