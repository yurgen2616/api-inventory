package com.drogueria.inventario.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.drogueria.inventario.models.Role;
import com.drogueria.inventario.models.Permission;
import com.drogueria.inventario.repositories.RoleRepository;
import com.drogueria.inventario.repositories.PermissionRepository;
import com.drogueria.inventario.services.RoleService;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Role createRole(Role role) {
        // Convertir a mayúsculas y agregar el prefijo ROLE_
        String formattedRoleName = "ROLE_" + role.getName().toUpperCase();
        role.setName(formattedRoleName);
        
        if (roleRepository.findByName(formattedRoleName).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existingRole = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Convertir a mayúsculas y agregar el prefijo ROLE_
        String formattedRoleName = "ROLE_" + role.getName().toUpperCase();
        
        if (!existingRole.getName().equals(formattedRoleName) && 
            roleRepository.findByName(formattedRoleName).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }

        existingRole.setName(formattedRoleName);
        existingRole.setDescription(role.getDescription());
        
        return roleRepository.save(existingRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
            
        if (!role.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete role: still assigned to users");
        }
        
        roleRepository.deleteById(id);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public void addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
            
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
            
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
    }
}