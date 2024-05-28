package org.example.service;

import org.example.domain.UserDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void findAllTest(){
        List<UserDTO> userDTOList = (List<UserDTO>) userService.findAll();
        System.out.println(userDTOList);
    }

    @Test
    public void existByIdTest(){
        boolean exist = userService.existById(11L);
        System.out.println("数据是否存在："+ exist);
    }
}
