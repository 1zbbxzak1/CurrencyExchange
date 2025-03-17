package ru.julia.currencyexchange.repository.interfaces;

import java.util.List;

public interface CrudRepository<T, ID> {
    void create(T entity);

    T read(ID id);

    List<T> readAll();
}
