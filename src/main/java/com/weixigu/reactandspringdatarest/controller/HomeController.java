package com.weixigu.reactandspringdatarest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class HomeController {

    @RequestMapping(value = {"/", "/tictactoe*", "/playerlist*", "about*"})
    public String directToView() {
        return "index";
    }
}
