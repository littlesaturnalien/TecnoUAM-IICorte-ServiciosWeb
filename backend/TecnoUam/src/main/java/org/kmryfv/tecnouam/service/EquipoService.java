package org.kmryfv.tecnouam.service;

import org.kmryfv.tecnouam.dto.EquipoDTO;
import org.kmryfv.tecnouam.exception.BusinessConflictException;
import org.kmryfv.tecnouam.exception.ResourceNotFoundException;
import org.kmryfv.tecnouam.mapper.EquipoMapper;
import org.kmryfv.tecnouam.model.Equipo;
import org.kmryfv.tecnouam.model.EstadoPrestamo;
import org.kmryfv.tecnouam.repository.EquipoRepository;
import org.kmryfv.tecnouam.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService extends GenericService<
        Equipo, EquipoDTO,
        EquipoRepository,
        EquipoMapper> {

    private final PrestamoRepository prestamoRepository;

    protected EquipoService(EquipoRepository repository, EquipoMapper mapper,
                            PrestamoRepository prestamoRepository) {
        super(repository, mapper);
        this.prestamoRepository = prestamoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EquipoDTO findById(Long id) {
        Equipo entity = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("No existe el equipo con ID: " + id));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No existe el equipo con ID: " + id);
        }
        boolean tienePrestamosActivos = prestamoRepository.existsByEquipoIdAndEstadoPrestamo(id, EstadoPrestamo.ACTIVO);
        if (tienePrestamosActivos) {
            throw new BusinessConflictException("No se puede eliminar el equipo porque tiene préstamos activos.");
        }

        repository.deleteById(id);
    }

}
