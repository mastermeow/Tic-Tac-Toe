package com.weixigu.boardgame.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests to the relative path of the React component <Home/>, <TicTacToe/>, <PlayerList/>, or <About/>
 * by returning the name of the view (index.html) of the React app.
 */
@Controller
public final class HomeController {

    @RequestMapping(value = {"/", "/tictactoe*", "/playerlist*", "about*"})
    public String directToView() {
        return "index";
    }
}
