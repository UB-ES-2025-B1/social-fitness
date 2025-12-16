package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock
  UserRepository userRepository;

  @InjectMocks
  UserDetailsServiceImpl service;

  @Test
  void loadUserByUsername_returnsUser_whenFound() {
    User user = new User("alice", "a@b.com", "p");
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

    assertSame(user, service.loadUserByUsername("alice"));
  }

  @Test
  void loadUserByUsername_throws_whenMissing() {
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
    assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
  }
}

