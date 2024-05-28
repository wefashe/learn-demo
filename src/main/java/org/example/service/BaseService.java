package org.example.service;

import org.example.domain.BaseDTO;

public interface BaseService<T extends BaseDTO, ID> {

    /**
     * 通过单个ID获取单个数据
     * @param id 主键ID
     * @return 单个数据
     */
    T findById(ID id);

    /**
     * 通过多个ID获取多个数据
     * @param ids 多个主键ID
     * @return 多个数据
     */
    Iterable<T> findById(Iterable<ID> ids);

    /**
     * 获取所有数据
     * @return 所有数据
     */
    Iterable<T> findAll();

    /**
     * 新增单个数据
     * @param entity 单个新增对象数据
     * @return 单个新增的数据
     * @param <S>
     */
    <S extends T> T add(T entity);

    /**
     * 新增多个数据
     * @param entities 多个新增对象数据
     * @return 多个新增的数据
     * @param <S>
     */
    <S extends T> Iterable<T> addList(Iterable<T> entities);

}
