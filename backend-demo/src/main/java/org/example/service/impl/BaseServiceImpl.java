package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.BaseRepository;
import org.example.domain.dos.BaseDO;
import org.example.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Objects;

@Slf4j
public abstract class BaseServiceImpl<M extends BaseRepository<T, ID>, T extends BaseDO<ID>, ID extends Number> implements BaseService<T, ID> {

    @Autowired
    protected M repository;

    @Override
    public T findById(ID id) {
        if (Objects.isNull(id)) {
            log.error("id为空");
            return null;
        }
        return repository.findById(id).orElse(null);
    }

    @Override
    public Iterable<T> findById(Iterable<ID> ids) {
        if (Objects.isNull(ids) || !ids.iterator().hasNext()) {
            log.error("id为空");
            return Collections.EMPTY_LIST;
        }
        return repository.findAllById(ids);
    }

    @Override
    public Iterable<T> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends T> T add(T entity) {
        if (Objects.isNull(entity)) {
            log.error("新增数据为空");
            return null;
        }
        if (Objects.nonNull(entity.getId())) {
            log.error("新增数据存在ID：{}", entity.getId());
            return null;
        }
        return repository.save(entity);
    }

    @Override
    public <S extends T> Iterable<T> add(Iterable<T> entities) {
        if (Objects.isNull(entities) || !entities.iterator().hasNext()) {
            log.error("新增数据为空");
            return Collections.EMPTY_LIST;
        }
        for (T entity : entities) {
            if (Objects.isNull(entity)) {
                log.error("新增数据存在为空");
                return Collections.EMPTY_LIST;
            }
            if (Objects.nonNull(entity.getId())) {
                log.error("新增数据存在ID：{}", entity.getId());
                return Collections.EMPTY_LIST;
            }
        }
        return repository.saveAll(entities);
    }

    @Override
    public long deleteById(ID id) {
        long count = 0;
        repository.deleteById(id);
        count++;
        return count;
    }

    @Override
    public long deleteByIds(Iterable<ID> ids) {
        long count = 0;
        repository.deleteAllById(ids);
        for (ID id : ids) {
            count++;
        }
        return count;
    }

    @Override
    public long deleteAll() {
        long count = this.count();
        repository.deleteAll();
        return count;
    }

    @Override
    public <S extends T> T update(T entity) {
        if (Objects.isNull(entity)) {
            log.error("修改数据为空");
            return null;
        }
        if (Objects.isNull(entity.getId())) {
            log.error("修改数据ID不存在");
            return null;
        }
        return repository.save(entity);
    }

    @Override
    public <S extends T> Iterable<T> update(Iterable<T> entities) {
        if (Objects.isNull(entities) || !entities.iterator().hasNext()) {
            log.error("修改数据为空");
            return Collections.EMPTY_LIST;
        }
        for (T entity : entities) {
            if (Objects.isNull(entity)) {
                log.error("修改数据存在为空");
                return Collections.EMPTY_LIST;
            }
            if (Objects.isNull(entity.getId())) {
                log.error("修改数据ID存在为空");
                return Collections.EMPTY_LIST;
            }
        }
        return repository.saveAll(entities);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public boolean existById(ID id) {
        if (Objects.isNull(id)) {
            log.error("id为空");
            return false;
        }
        return repository.existsById(id);
    }

}
