package ru.julia.currencyexchange.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import ru.julia.currencyexchange.entity.enums.RoleEnum;

import java.util.List;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleEnum roleName;

    @OneToMany(mappedBy = "role")
    @JsonBackReference
    private List<UserRole> userRoles;

    public Role() {
    }

    public Role(RoleEnum roleName) {
        this.roleName = roleName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName.getRoleName();
    }

    public void setRoleName(RoleEnum roleName) {
        this.roleName = roleName;
    }


    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
}
