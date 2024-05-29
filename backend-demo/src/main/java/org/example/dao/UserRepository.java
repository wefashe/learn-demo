package org.example.dao;

import org.example.domain.dos.UserDO;

public interface UserRepository extends BaseRepository<UserDO, Long> {

    UserDO findByUsernameAndPassword(String username, String password);
}
