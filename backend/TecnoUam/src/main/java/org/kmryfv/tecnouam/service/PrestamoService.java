package org.kmryfv.tecnouam.service;

import org.kmryfv.tecnouam.dto.PrestamoDTO;
import org.kmryfv.tecnouam.exception.BadRequestException;
import org.kmryfv.tecnouam.exception.BusinessConflictException;
import org.kmryfv.tecnouam.exception.ResourceNotFoundException;
import org.kmryfv.tecnouam.mapper.PrestamoMapper;
import org.kmryfv.tecnouam.model.Equipo;
import org.kmryfv.tecnouam.model.EstadoEquipo;
import org.kmryfv.tecnouam.model.EstadoPrestamo;
import org.kmryfv.tecnouam.model.Prestamo;
import org.kmryfv.tecnouam.repository.EquipoRepository;
import org.kmryfv.tecnouam.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoService extends GenericService<
        Prestamo,
        PrestamoDTO,
        PrestamoRepository,
        PrestamoMapper> {

    private final EquipoRepository equipoRepository;

    public PrestamoService(PrestamoRepository repository,
                           PrestamoMapper mapper,
                           EquipoRepository equipoRepository) {
        super(repository, mapper);
        this.equipoRepository = equipoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoDTO findById(Long id) {
        Prestamo entity = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("No existe el préstamo con ID: " + id));
        return mapper.toDto(entity);
    }

    @Transactional
    public PrestamoDTO registrarPrestamo(PrestamoDTO dto) {
        if (dto.equipoId() == null) {
            throw new BadRequestException("Debe indicar el ID del equipo (equipoId).");
        }

        if (dto.fechaDevolucion() != null && dto.fechaPrestamo() != null) {
            if (dto.fechaDevolucion().isBefore(dto.fechaPrestamo())) {
                throw new BadRequestException("La fecha prevista de devolución no puede ser anterior a la fecha del préstamo.");
            }
        }

        Equipo equipo = equipoRepository.findById(dto.equipoId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el equipo con ID: " + dto.equipoId()));

        if (equipo.getEstado() == EstadoEquipo.PRESTADO) {
            throw new BusinessConflictException("El equipo ya se encuentra PRESTADO y no está disponible.");
        }

        equipo.setEstado(EstadoEquipo.PRESTADO);
        equipoRepository.save(equipo);

        Prestamo prestamo = mapper.toEntity(dto);
        prestamo.setEquipo(equipo);
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);

        return mapper.toDto(repository.save(prestamo));
    }

    @Transactional
    public PrestamoDTO registrarDevolucion(Long prestamoId) {
        Prestamo prestamo = repository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el préstamo con ID: " + prestamoId));

        if (prestamo.getEstadoPrestamo() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessConflictException("El préstamo ya se encuentra registrado como DEVUELTO.");
        }

        prestamo.setEstadoPrestamo(EstadoPrestamo.DEVUELTO);

        Equipo equipo = prestamo.getEquipo();
        if (equipo != null) {
            equipo.setEstado(EstadoEquipo.DISPONIBLE);
            equipoRepository.save(equipo);
        }

        return mapper.toDto(repository.save(prestamo));
    }
}