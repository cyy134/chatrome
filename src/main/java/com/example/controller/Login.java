package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/index")
public class Login {

    @RequestMapping("/login")
    public String toLogin(){
        return "web";
    }
}
