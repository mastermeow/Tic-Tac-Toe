package com.weixigu.reactandspringdatarest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

//Unit Test for @Controller class
class HomeControllerUnitTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        HomeController homeController = new HomeController();
        this.mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    void testMockSetUp(){
        assertThat(this.mockMvc).isNotNull();
    }

    @Test
    void directToView_validRelativePaths_shouldSucceed() throws Exception{

        String[] myPaths = {"/", "/tictactoe*", "/playerlist*", "/about*"};

        for(String myPath : myPaths){
            this.mockMvc.perform(get(myPath)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"))
                    .andExpect(forwardedUrl("index"))
                    .andExpect(redirectedUrl(null));
        }
    }
}
