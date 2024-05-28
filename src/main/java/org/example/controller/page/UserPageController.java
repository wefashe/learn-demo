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

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;

@Controller
public class UserPageController {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession session;

    @GetMapping("userList")
    public String userList(Model model) {
        UserDO userDO = (UserDO) session.getAttribute("user");
        if (Objects.isNull(userDO)) {
            return "redirect:/";
        } else {
            List<UserDO> users = (List<UserDO>) userService.findAll();
            model.addAttribute("users", users);
            return "user/userList";
        }
    }

    @GetMapping("userAdd")
    public String userAdd() {
        return "user/userUpdate";
    }

    @GetMapping("userUpdate/{id}")
    public String userUpdate(@PathVariable Long id, Model model) {
        UserDO userDO = (UserDO) session.getAttribute("user");
        if (Objects.isNull(userDO)) {
            return "redirect:/";
        } else {
            model.addAttribute("user",userService.findById(id));
            return "user/userUpdate";
        }
    }

    @PostMapping("userSave")
    public String save(UserDO user) {
        if (Objects.nonNull(user)) {
            if (Objects.nonNull(user.getId())) {
                userService.update(user);
            } else {
                userService.add(user);
            }
        }
        return "redirect:userList";
    }

    @GetMapping("userDelete/{id}")
    public String delete(@PathVariable Long id) {
        UserDO userDO = (UserDO) session.getAttribute("user");
        if (!id.equals(userDO.getId())) {
            userService.deleteById(id);
        }
        return "forward:userList";
    }
}
