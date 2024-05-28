package org.example.controller.page;

import org.example.annotation.NoResponseBody;
import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Objects;

@Controller
public class UserPageController {

    @Autowired
    private UserService userService;

    @GetMapping("userAdd")
    @NoResponseBody
    public String userAdd() {
        return "user/userUpdate";
    }

    @GetMapping("userUpdate/{id}")
    public String userUpdate(@PathVariable Long id, Model model) {
        model.addAttribute("user",userService.findById(id));
        return "user/userUpdate";
    }

    @PostMapping("userSave")
    public String save(UserDO user, Model model) {
        if (Objects.nonNull(user)) {
            if (Objects.nonNull(user.getId())) {
                userService.update(user);
            } else {
                userService.add(user);
            }
        }
        return "redirect:/";
    }

    @GetMapping("userDelete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/";
    }
}
