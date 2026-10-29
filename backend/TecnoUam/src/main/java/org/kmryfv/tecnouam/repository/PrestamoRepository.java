package org.kmryfv.tecnouam.repository;

import org.kmryfv.tecnouam.model.EstadoPrestamo;
import org.kmryfv.tecnouam.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo,Long> {
    boolean existsByEquipoIdAndEstadoPrestamo(Long equipoId, EstadoPrestamo estado);
}
