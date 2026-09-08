package org.schemeguard.backend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class newtest {

    @PostMapping("test2")
    public String test2(
            @RequestParam String var
    ) {
        return var + " res";
    }
}
