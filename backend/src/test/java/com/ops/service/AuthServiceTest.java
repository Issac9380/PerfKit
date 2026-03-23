package com.ops.service;

import com.ops.common.BusinessException;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.entity.User;
import com.ops.mapper.UserMapper;
import com.ops.security.JwtTokenProvider;
import com.ops.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@test.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setPassword("encodedPassword");
        existingUser.setStatus("ACTIVE");
    }

    @Test
    void testRegister_Success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> authService.register(registerRequest));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateToken(anyLong(), anyString())).thenReturn("test-token");

        String token = authService.login(loginRequest);

        assertNotNull(token);
        assertEquals("test-token", token);
    }

    @Test
    void testLogin_WrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_UserInactive() {
        existingUser.setStatus("INACTIVE");
        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("账户已被禁用", exception.getMessage());
    }
}
