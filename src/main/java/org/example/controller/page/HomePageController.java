package org.example.controller.page;

import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class HomePageController {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession session;

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @PostMapping("login")
    public String login(UserDO userDO) {
        UserDO user = userService.login(userDO);
        if(user != null){
            session.setAttribute("user", user);
            return "redirect:userList";
        }else{
            return "redirect:/";
        }
    }

    @GetMapping("logout")
    public String logout(UserDO userDO, HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

}
