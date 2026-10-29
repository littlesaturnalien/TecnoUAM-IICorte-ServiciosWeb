package org.kmryfv.tecnouam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade=CascadeType.ALL)
    private Equipo equipo;

    @Column(nullable=false, length=100)
    private String solicitante;

    @Column(nullable=false)
    private LocalDate fechaPrestamo = LocalDate.now();

    @Column(nullable=false)
    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=8)
    private EstadoPrestamo estadoPrestamo;
}
