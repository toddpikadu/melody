package com.company.production.repository;

import com.company.production.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // 根据角色名称查找角色
    Role findByName(String name);
}
