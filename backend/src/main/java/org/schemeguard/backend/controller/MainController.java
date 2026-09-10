package org.schemeguard.backend.controller;

import org.schemeguard.backend.entity.Role;
import org.schemeguard.backend.repository.RolesRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class MainController {

    private final RolesRepository rolesRepository;
    private final Logger logger = Logger.getLogger(MainController.class.getName());

    public MainController(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "Hello, World!";
    }

    @GetMapping("/test")
    public Iterable<Role> getRole(
            @RequestParam(required = false) String name
    ) {
        if(name == null) {
            return rolesRepository.findAll();
        }
        return List.of(rolesRepository.findRoleByName(name));
    }
}