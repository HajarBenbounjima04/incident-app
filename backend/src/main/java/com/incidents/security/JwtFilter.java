package com.incidents.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {
    public static final String ERROR_ATTR = "jwt_error";
    private final JwtService jwt;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtService jwt, UserDetailsService uds) { this.jwt = jwt; this.userDetailsService = uds; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return req.getRequestURI().startsWith("/auth/") || req.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwt.extractEmail(header.substring(7));
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Les droits sont relus en base : desactivation / changement de role immediatement pris en compte.
                    UserDetails ud = userDetailsService.loadUserByUsername(email);
                    if (ud.isEnabled()) {
                        var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        req.setAttribute(ERROR_ATTR, "Compte desactive");
                    }
                }
            } catch (ExpiredJwtException e) {
                req.setAttribute(ERROR_ATTR, "Token expire");
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                req.setAttribute(ERROR_ATTR, "Token invalide");
            }
        }
        chain.doFilter(req, res);
    }
}
