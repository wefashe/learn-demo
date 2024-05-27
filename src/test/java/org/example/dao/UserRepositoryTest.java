package org.example.dao;


import org.example.domain.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    @Commit
    public void addUserTest(){
        UserDTO user = UserDTO.builder()
                .nickName("李元芳")
                .username("wefashe")
                .email("yuanfang@qq.com")
                .build();
        repository.save(user);
    }
}
