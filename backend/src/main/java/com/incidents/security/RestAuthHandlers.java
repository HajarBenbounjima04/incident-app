package com.incidents.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Reponses JSON propres pour 401 (non authentifie / token expire) et 403 (role insuffisant) au niveau filtre. */
@Component
public class RestAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        Object reason = req.getAttribute(JwtFilter.ERROR_ATTR);
        write(res, HttpStatus.UNAUTHORIZED, reason != null ? reason.toString() : "Authentification requise");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        write(res, HttpStatus.FORBIDDEN, "Acces interdit");
    }

    private void write(HttpServletResponse res, HttpStatus s, String msg) throws IOException {
        res.setStatus(s.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), Map.of("status", s.value(), "message", msg, "timestamp", LocalDateTime.now().toString()));
    }
}
