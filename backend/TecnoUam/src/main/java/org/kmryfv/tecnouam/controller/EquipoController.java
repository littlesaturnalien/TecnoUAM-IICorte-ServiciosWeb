package org.kmryfv.tecnouam.controller;

import org.kmryfv.tecnouam.dto.EquipoDTO;
import org.kmryfv.tecnouam.service.EquipoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public ResponseEntity<List<EquipoDTO>> findAll() {
        return ResponseEntity.ok(equipoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EquipoDTO> create(@RequestBody EquipoDTO dto) {
        EquipoDTO nuevoEquipo = equipoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEquipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> update(@PathVariable Long id, @RequestBody EquipoDTO dto) {
        return ResponseEntity.ok(equipoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}