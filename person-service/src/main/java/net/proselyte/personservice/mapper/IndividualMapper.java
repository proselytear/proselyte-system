package net.proselyte.personservice.mapper;


import lombok.Setter;
import net.proselyte.person.dto.IndividualDto;
import net.proselyte.person.dto.IndividualWriteDto;

import net.proselyte.personservice.entity.Individual;
import net.proselyte.personservice.util.DateTimeUtil;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.springframework.util.CollectionUtils.isEmpty;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = {
                UserMapper.class,
                AddressMapper.class,
        }
)
@Setter(onMethod_ = @Autowired)
public abstract class IndividualMapper {

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "user", source = ".", qualifiedByName = "toUser")
    public abstract Individual to(IndividualWriteDto dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "address", source = "user.address", qualifiedByName = "fromAddress")
    public abstract IndividualDto from(Individual individual);

    public List<IndividualDto> from(List<Individual> individuals) {
        return isEmpty(individuals)
                ? Collections.emptyList()
                : individuals.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "user", expression = "java(userMapper.update(individual, dto))")
    public abstract void update(
            @MappingTarget
            Individual individual,
            IndividualWriteDto dto
    );
}