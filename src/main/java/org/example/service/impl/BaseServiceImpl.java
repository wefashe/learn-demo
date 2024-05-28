package org.example.service.impl;

import org.example.dao.BaseRepository;
import org.example.domain.BaseDTO;
import org.example.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseServiceImpl<T extends BaseDTO, ID> implements BaseService<T, ID> {

    @Autowired
    private BaseRepository<T, ID> repository;

    @Override
    public T findById(ID id) {
        return repository.findById(id).get();
    }

    @Override
    public Iterable<T> findById(Iterable<ID> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public Iterable<T> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends T> T add(T entity) {
        return repository.save(entity);
    }

    @Override
    public <S extends T> Iterable<T> addList(Iterable<T> entities) {
        return repository.saveAll(entities);
    }
}
