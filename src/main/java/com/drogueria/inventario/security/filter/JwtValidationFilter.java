package com.drogueria.inventario.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.drogueria.inventario.services.PermissionsCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.drogueria.inventario.security.TokenJwtConfig.*;

public class JwtValidationFilter extends BasicAuthenticationFilter {
    
    private final PermissionsCacheService permissionsCacheService;

    public JwtValidationFilter(AuthenticationManager authenticationManager, 
                             PermissionsCacheService permissionsCacheService) {
        super(authenticationManager);
        this.permissionsCacheService = permissionsCacheService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        
        String header = request.getHeader(HEADER_AUTHORIZATION);
        
        if (header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = header.replace(PREFIX_TOKEN, "");
        
        try {
            Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
                
            String username = claims.getSubject();
            
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            
            if (roles == null) {
                roles = new ArrayList<>();
            }
            
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            roles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role));
                Collection<GrantedAuthority> rolePermissions = permissionsCacheService.getPermissionsForRole(role);
                authorities.addAll(rolePermissions);
            });
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            
            // Verificar si la respuesta ya ha sido comprometida
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(CONTENT_TYPE);
                
                Map<String, String> body = new HashMap<>();
                body.put("error", e.getMessage());
                body.put("message", "El token es inválido!");
                
                try {
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                } catch (IOException ex) {
                    logger.error("Error writing error response", ex);
                }
            }
            
            // No llamar a chain.doFilter si la autenticación falló
            return;
        }
    }
}