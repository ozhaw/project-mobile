package org.nure.julia.mapper;

public interface Mapper<T, R> {

    R map(T t);

}
