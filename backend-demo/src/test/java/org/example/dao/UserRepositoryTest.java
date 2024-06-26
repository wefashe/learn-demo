package org.example.dao;


import org.example.domain.dos.UserDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    public void addUserTest(){
        UserDO user = UserDO.builder()
                .nickName("李元芳")
                .username("wefashe12")
                .password("sdsdhfks")
                .email("yuanfang@qq.com")
                .build();
        // 未设置id是新增
        repository.save(user);
    }

    @Test
    public void updateUserTest(){
        List<UserDO> userList = repository.findAll();
        if(userList == null || userList.size() == 0){
            return;
        }
        UserDO user = userList.get(0);
        user.setNickName(user.getNickName() + "-test");
        // 设置了id就是修改
        repository.save(user);
    }
}
