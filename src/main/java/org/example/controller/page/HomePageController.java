package org.example.controller.page;

import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomePageController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        List<UserDO> users = (List<UserDO>) userService.findAll();
        model.addAttribute("users",users);
        return "user/userList";
    }

}
