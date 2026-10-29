package org.kmryfv.tecnouam.mapper;

import org.kmryfv.tecnouam.dto.EquipoDTO;
import org.kmryfv.tecnouam.model.Equipo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EquipoMapper extends GenericMapper<Equipo, EquipoDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(EquipoDTO dto, @MappingTarget Equipo entity);
}
