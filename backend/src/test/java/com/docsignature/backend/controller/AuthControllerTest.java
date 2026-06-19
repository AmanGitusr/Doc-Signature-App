package com.docsignature.backend.controller;

import com.docsignature.backend.dto.AuthDtos;
import com.docsignature.backend.security.JwtAuthenticationFilter;
import com.docsignature.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean AuthService authService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registerReturnsToken() throws Exception {
        when(authService.register(any())).thenReturn(new AuthDtos.AuthResponse("token", new AuthDtos.UserResponse(1L, "Alice", "alice@example.com", "OWNER")));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Alice","email":"alice@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.user.email").value("alice@example.com"));
    }
}
