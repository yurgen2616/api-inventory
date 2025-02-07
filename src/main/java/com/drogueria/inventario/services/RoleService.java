package com.drogueria.inventario.services;

import com.drogueria.inventario.models.Role;
import java.util.List;

public interface RoleService {
    Role createRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
    Role getRoleById(Long id);
    List<Role> getAllRoles();
    void addPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);
}