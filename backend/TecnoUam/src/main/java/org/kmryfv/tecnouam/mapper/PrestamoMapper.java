package org.kmryfv.tecnouam.mapper;

import org.kmryfv.tecnouam.dto.PrestamoDTO;
import org.kmryfv.tecnouam.model.Equipo;
import org.kmryfv.tecnouam.model.Prestamo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class,
        imports = Equipo.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrestamoMapper extends GenericMapper<Prestamo, PrestamoDTO> {
    @Override
    @Mapping(source = "equipo.id", target = "equipoId")
    PrestamoDTO toDto(Prestamo entity);

    @Override
    @Mapping(source = "equipoId", target = "equipo.id")
    Prestamo toEntity(PrestamoDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(PrestamoDTO dto, @MappingTarget Prestamo entity);
}
