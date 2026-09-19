package com.perfumes.nuochoa.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý trang chủ của website.
 */
@Controller
public class HomeController {

    /** Hiển thị trang chủ. Cả "/" và "/home" đều dẫn tới template web/index.html. */
    @GetMapping({"/", "/home"})
    public String homePage() {
        return "web/index";
    }
}