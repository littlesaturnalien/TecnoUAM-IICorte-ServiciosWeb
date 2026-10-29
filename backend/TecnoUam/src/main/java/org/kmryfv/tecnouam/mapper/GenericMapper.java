package org.kmryfv.tecnouam.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface GenericMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entityList);
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
