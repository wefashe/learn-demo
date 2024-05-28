package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.dos.BaseDO;
import org.example.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public abstract class BaseController<T extends BaseDO, ID> {

    @Autowired
    protected BaseService<T, ID> service;

    @PostMapping("add")
    public T add(@RequestBody T entity){
        return service.add(entity);
    }

    @GetMapping("delete/{id}")
    public long delete(@PathVariable ID id){
        return service.deleteById(id);
    }

    @PostMapping("update")
    public T update(@RequestBody T entity){
        return service.update(entity);
    }

    @GetMapping("find/{id}")
    public T find(@PathVariable ID id){
        return service.findById(id);
    }
}
