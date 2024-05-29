package org.example.service.impl;

import org.example.dao.UserRepository;
import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserDO, Long> implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDO login(UserDO userDO) {
        return userRepository.findByUsernameAndPassword(userDO.getUsername(), userDO.getPassword());
    }

}
