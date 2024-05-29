package org.example.dao;

import org.example.domain.dos.BaseDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRepository<T extends BaseDO<ID>, ID extends Number> extends JpaRepository<T, ID> {

}
