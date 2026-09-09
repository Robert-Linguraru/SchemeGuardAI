package org.schemeguard.backend.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Id;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="roles")
public class Role {
    @Id
    private UUID id;
    private String name;

}
