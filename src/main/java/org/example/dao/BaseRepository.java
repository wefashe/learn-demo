package org.example.dao;

import org.example.domain.BaseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRepository<T extends BaseDTO, ID> extends JpaRepository<T, ID> {
}
