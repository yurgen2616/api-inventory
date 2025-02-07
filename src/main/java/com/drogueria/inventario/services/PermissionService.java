package com.drogueria.inventario.services;

import com.drogueria.inventario.models.Permission;
import java.util.List;

public interface PermissionService {
    Permission createPermission(Permission permission);
    Permission updatePermission(Long id, Permission permission);
    void deletePermission(Long id);
    Permission getPermissionById(Long id);
    List<Permission> getAllPermissions();
}