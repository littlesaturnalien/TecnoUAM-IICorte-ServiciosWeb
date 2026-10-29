package org.kmryfv.tecnouam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String codigo;

    @Column(nullable=false, length=100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=9)
    private TipoEquipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private EstadoEquipo estado;
}
