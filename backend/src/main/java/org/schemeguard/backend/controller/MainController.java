package org.schemeguard.backend.controller;

import org.schemeguard.backend.dto.Role;
import org.schemeguard.backend.repository.RolesRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final RolesRepository rolesRepository;

    public MainController(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "Hello, World!";
    }

    @GetMapping("/test")
    public Role getRole(
            @RequestParam String name
    ) {
        return rolesRepository.findRoleByName(name);
    }

}
