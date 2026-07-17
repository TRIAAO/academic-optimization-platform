package com.triacompany.academic.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Test
    void shouldReturnUnauthorizedWhenJwtIsExpiredOrInvalid() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean filterChainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (servletRequest, servletResponse) -> filterChainCalled.set(true);

        when(jwtService.extractSubject("expired-token"))
                .thenThrow(new JwtException("expired"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("Sessão expirada ou token inválido"));
        assertFalse(filterChainCalled.get());
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean filterChainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (servletRequest, servletResponse) -> filterChainCalled.set(true);

        filter.doFilter(request, response, filterChain);

        assertTrue(filterChainCalled.get());
        assertEquals(200, response.getStatus());
    }
}
