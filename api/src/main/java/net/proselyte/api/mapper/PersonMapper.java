package net.proselyte.api.mapper;

import net.proselyte.individual.dto.IndividualDto;
import net.proselyte.individual.dto.IndividualWriteDto;
import net.proselyte.individual.dto.IndividualWriteResponseDto;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {

    net.proselyte.person.dto.IndividualWriteDto from(IndividualWriteDto dto);

    net.proselyte.person.dto.IndividualDto from(IndividualDto dto);

    IndividualDto from(net.proselyte.person.dto.IndividualDto dto);

    IndividualWriteResponseDto from(net.proselyte.person.dto.IndividualWriteResponseDto dto);
}
