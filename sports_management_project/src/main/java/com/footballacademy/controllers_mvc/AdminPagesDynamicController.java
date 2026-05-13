package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public
class AdminPagesDynamicController {
    @GetMapping({
        "/reports/view/index", "/admin/view/reports/index"
    })
    public String reports() {
        return "pages/modules/reports/index";
    }
    @GetMapping({
        "/settings/view/academy", "/admin/view/settings/academy"
    })
    public String academySettings() {
        return "pages/modules/settings/academy";
    }
    @GetMapping({
        "/home/view/index", "/home/view/index-sport"
    })
    public String homeAlias() {
        return "pages/modules/home/index";
    }
}
