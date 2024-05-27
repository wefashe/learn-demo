package org.example.service;

import org.example.domain.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DataJpaTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void addUserTest(){
        UserDTO user = UserDTO.builder()
                .nickName("李元芳")
                .username("wefashe")
                .email("yuanfang@qq.com")
                .build();
        userService.addUser(user);
    }
}
