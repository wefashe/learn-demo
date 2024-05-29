package org.example.service;

import org.example.domain.dos.BaseDO;

public interface BaseService<T extends BaseDO<ID>, ID extends Number> {

    /**
     * 通过单个ID获取单个数据
     * @param id 主键ID
     * @return 成功获取的数据
     */
    T findById(ID id);

    /**
     * 通过多个ID获取多个数据
     * @param ids 多个主键ID
     * @return 成功获取的数据
     */
    Iterable<T> findById(Iterable<ID> ids);

    /**
     * 获取所有数据
     * @return 成功获取的数据
     */
    Iterable<T> findAll();

    /**
     * 新增单个数据
     * @param entity 单个新增对象数据
     * @return 成功新增的数据
     * @param <S>
     */
    <S extends T> T add(T entity);

    /**
     * 新增多个数据
     * @param entities 多个新增对象数据
     * @return 成功新增的数据
     * @param <S>
     */
    <S extends T> Iterable<T> add(Iterable<T> entities);

    /**
     * 通过单个ID删除单个数据
     * @param id 主键ID
     * @return 成功删除的数量
     */
    long deleteById(ID id);

    /**
     * 通过多个ID删除多个数据
     * @param ids 多个主键ID
     * @return 成功删除的数量
     */
    long deleteByIds(Iterable<ID> ids);

    /**
     * 删除所有数据
     * @return 成功删除的数量
     */
    long deleteAll();

    /**
     * 修改单个数据
     * @param entity 单个修改的数据
     * @return 成功修改的数据
     * @param <S>
     */
    <S extends T> T  update(T entity);

    /**
     * 修改多个数据
     * @param entities 多个修改对象数据
     * @return 成功修改的数据
     * @param <S>
     */
    <S extends T> Iterable<T> update(Iterable<T> entities);

    /**
     * 获取所有数据个数
     * @return 数据个数
     */
    long count();

    /**
     * 通过单个ID判断单个数据是否存在
     * @param id 主键ID
     * @return 数据是否存在
     */
    boolean existById(ID id);

}
