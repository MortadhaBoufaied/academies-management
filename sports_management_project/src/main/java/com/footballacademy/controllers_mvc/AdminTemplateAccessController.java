package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public
class AdminTemplateAccessController {
    @GetMapping({
        "/admin/view/page-index", "/admin/view/resource-index", "/admin/view/component-library"
    })
    public String index() {
        return "pages/modules/admin/page-index";
    }
}
