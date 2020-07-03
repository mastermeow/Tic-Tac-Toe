package com.weixigu.reactandspringdatarest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

//Web MVC test of the HomeController class. Loads only the web layer.
@WebMvcTest(HomeController.class)
class HomeControllerWebMvcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    @Autowired //Let Spring instantiate the bean.
    private MockMvc mockMvc;

    @Test
    void directToView_validRelativePaths_shouldSucceed() throws Exception{

        LOGGER.info("WebMvcTest: directToView_validRelativePaths_shouldSucceed()");

        String[] myPaths = {"/", "/tictactoe*", "/playerlist*", "/about*"};

        for(String myPath : myPaths){
            this.mockMvc.perform(get(myPath))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                    .andExpect(view().name("index"))
                    .andExpect(redirectedUrl(null))
                    .andExpect(forwardedUrl(null));
        }
    }
}
