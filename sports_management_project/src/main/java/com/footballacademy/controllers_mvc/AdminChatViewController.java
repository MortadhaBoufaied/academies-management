package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public
class AdminChatViewController {
    @GetMapping({
        "/admin/view/chat", "/chat/view/hub", "/admin/view/chat/hub"
    })
    public String hub() {
        return "pages/modules/chat/hub";
    }
    @GetMapping("/admin/view/chat/console")
    public String console() {
        return "pages/modules/chat/bot-console";
    }
}
