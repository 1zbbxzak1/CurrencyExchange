package ru.julia.currencyexchange.entity;

import jakarta.persistence.*;
import ru.julia.currencyexchange.entity.enums.RoleEnum;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleEnum role;

    public Role() {
    }

    public Role(RoleEnum role) {
        this.role = role;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
