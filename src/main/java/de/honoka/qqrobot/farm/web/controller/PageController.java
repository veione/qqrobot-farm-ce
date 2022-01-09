package de.honoka.qqrobot.farm.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {

    @RequestMapping("/text")
    public ModelAndView text(@RequestParam String text) {
        return new ModelAndView("text", "text",
                text.replace("\n", "<br>"));
    }

    @RequestMapping("/")
    public String root() {
        return "forward:/index.html";
    }
}
