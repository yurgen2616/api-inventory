package com.drogueria.inventario.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.drogueria.inventario.models.Permission;
import com.drogueria.inventario.repositories.PermissionRepository;
import com.drogueria.inventario.services.PermissionService;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Permission createPermission(Permission permission) {
        if (permissionRepository.findByName(permission.getName()).isPresent()) {
            throw new RuntimeException("Permission name already exists");
        }
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {
        Permission existingPermission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        if (!existingPermission.getName().equals(permission.getName()) && 
            permissionRepository.findByName(permission.getName()).isPresent()) {
            throw new RuntimeException("Permission name already exists");
        }

        existingPermission.setName(permission.getName());
        existingPermission.setDescription(permission.getDescription());
        
        return permissionRepository.save(existingPermission);
    }

    @Override
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
            
        if (!permission.getRoles().isEmpty()) {
            throw new RuntimeException("Cannot delete permission: still assigned to roles");
        }
        
        permissionRepository.deleteById(id);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
}