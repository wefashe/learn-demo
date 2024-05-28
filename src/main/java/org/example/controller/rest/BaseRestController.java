package org.example.controller.rest;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.dos.BaseDO;
import org.example.domain.groups.Add;
import org.example.domain.groups.Update;
import org.example.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@Slf4j
@RestController
public abstract class BaseRestController<T extends BaseDO, ID> {

    @Autowired
    protected BaseService<T, ID> service;

    @PostMapping("add")
    public T add(@RequestBody @Validated(Add.class) T entity){
        return service.add(entity);
    }

    @GetMapping("delete/{id}")
    public long delete(@PathVariable @NotNull(message = "id不能为空") ID id){
        return service.deleteById(id);
    }

    @PostMapping("update")
    public T update(@RequestBody @Validated(Update.class) T entity){
        return service.update(entity);
    }

    @GetMapping("find/{id}")
    public T find(@PathVariable @NotNull(message = "id不能为空") ID id){
        return service.findById(id);
    }
}
