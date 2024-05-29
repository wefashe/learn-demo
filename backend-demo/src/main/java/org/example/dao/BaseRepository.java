package org.example.dao;

import org.example.domain.dos.BaseDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRepository<T extends BaseDO, ID> extends JpaRepository<T, ID> {
}
