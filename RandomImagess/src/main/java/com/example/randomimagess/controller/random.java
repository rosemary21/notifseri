package com.example.randomimagess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/random")
public class random {

    @RequestMapping("/value")
    public String getRandomImages(){
        return "login";
    }
}

