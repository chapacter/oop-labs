package ru.ssau.tk.avokado.lab2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    @GetMapping("/")
    public ModelAndView root() {
        return new ModelAndView("redirect:/api");
    }

    @GetMapping("/api")
    public String apiRoot() {
        return "API endpoints are available at /api/";
    }
}