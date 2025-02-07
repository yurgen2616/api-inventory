package com.drogueria.inventario.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.drogueria.inventario.models.User;
import com.drogueria.inventario.repositories.UserRepository;

public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> userOptional;
        if (usernameOrEmail.contains("@")) {
            userOptional = repository.findByEmail(usernameOrEmail);
        } else {
            userOptional = repository.findByUsername(usernameOrEmail);
        }
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(
                    String.format("El Usuario %s no existe en el sistema", usernameOrEmail));
        }
        User user = userOptional.orElseThrow();

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> {
                    List<GrantedAuthority> auths = new ArrayList<>();
                    // Agregar permisos
                    role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                            .forEach(auths::add);
                    // Agregar rol
                    auths.add(new SimpleGrantedAuthority(role.getName()));
                    return auths.stream();
                })
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true,
                true,
                true,
                true, authorities);
    }

}
