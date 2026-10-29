package org.kmryfv.tecnouam.controller;

import org.kmryfv.tecnouam.dto.PrestamoDTO;
import org.kmryfv.tecnouam.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public ResponseEntity<List<PrestamoDTO>> findAll() {
        return ResponseEntity.ok(prestamoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PrestamoDTO> create(@RequestBody PrestamoDTO dto) {
        PrestamoDTO nuevoPrestamo = prestamoService.registrarPrestamo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPrestamo);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<PrestamoDTO> registrarDevolucion(@PathVariable Long id) {
        PrestamoDTO prestamoActualizado = prestamoService.registrarDevolucion(id);
        return ResponseEntity.ok(prestamoActualizado);
    }
}