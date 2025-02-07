package com.drogueria.inventario.controllers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drogueria.inventario.models.Permission;
import com.drogueria.inventario.models.User;
import com.drogueria.inventario.repositories.UserRepository;
import com.drogueria.inventario.services.PermissionsCacheService;

@RestController
@RequestMapping("/menu")
public class MenuController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PermissionsCacheService permissionsCacheService;
    
    @GetMapping("/access")
    public ResponseEntity<?> getUserPermissions(Authentication authentication) {
        // Forzar la recarga del caché
        permissionsCacheService.evictAllCache();
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .collect(Collectors.toSet());
            
        return ResponseEntity.ok(permissions);
    }
}