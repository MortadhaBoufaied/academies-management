package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public
class AdminChatbotViewController {
    @GetMapping({
        "/admin/view/chatbot", "/admin/view/chatbot/qa", "/chatbot/view/qa"
    })
    public String qa() {
        return "pages/modules/chatbot/qa";
    }
    @GetMapping("/admin/view/chatbot/console")
    public String console() {
        return "pages/modules/chat/bot-console";
    }
    @GetMapping("/admin/view/chatbot-data")
    public String knowledge() {
        return "pages/modules/chat/bot-knowledge";
    }
    @PostMapping("/admin/chatbot/ask")
    public String ask() {
        return "redirect:/admin/view/chatbot?submitted=true";
    }
}
