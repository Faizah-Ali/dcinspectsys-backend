package com.dhc.inspection_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = {"/", "/{path:[^\\.]*}"})
    public String redirect() {
        return "forward:/index.html";
    }
}