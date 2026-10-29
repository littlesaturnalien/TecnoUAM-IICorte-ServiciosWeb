package org.kmryfv.tecnouam.service;

import org.kmryfv.tecnouam.mapper.GenericMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class GenericService<E, D,
            R extends JpaRepository<E, Long>,
            M extends GenericMapper<E, D>>
    {

        protected final R repository;
        protected final M mapper;

        protected GenericService(R repository, M mapper) {
            this.repository = repository;
            this.mapper = mapper;
        }

        @Transactional(readOnly = true)
        public List<D> findAll() {
            return mapper.toDtoList(repository.findAll());
        }

        @Transactional(readOnly = true)
        public D findById(Long id) {
            E entity = repository.findById(id).orElseThrow(() ->
                    new RuntimeException("No existe el registro con ID: " + id));
            return mapper.toDto(entity);
        }

        @Transactional
        public D create(D dto) {
            E entity = mapper.toEntity(dto);
            return mapper.toDto(repository.save(entity));
        }

        @Transactional
        public D update(Long id, D dto) {
            D dtoResult = findById(id);
            return mapper.toDto(repository.save(mapper.toEntity(dto)));
        }

        @Transactional
        public void delete(Long id) {
            if (!repository.existsById(id)) throw new RuntimeException("No existe");
            repository.deleteById(id);
        }
    }
