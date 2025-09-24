package net.proselyte.api.environment.config.testcontainer.data;

import org.springframework.stereotype.Component;

import net.proselyte.individual.dto.AddressWriteDto;
import net.proselyte.individual.dto.IndividualWriteDto;
import net.proselyte.individual.dto.UserLoginRequest;


@Component
public class DtoCreator {
    public IndividualWriteDto buildIndividualWriteDto() {
        var address = new AddressWriteDto();
        address.setAddress("FAKE_STREET");
        address.setZipCode("FAKE_1111");
        address.setCity("FAKE_CITY");
        address.setCountryCode("RUS");

        var request = new IndividualWriteDto();
        request.setFirstName("JOHN");
        request.setLastName("DOE");
        request.setEmail("test@mail.com");
        request.setPassword("secret123");
        request.setConfirmPassword("secret123");
        request.passportNumber("FAKE_1234");
        request.phoneNumber("+70000000001");
        request.setAddress(address);

        return request;
    }

    public UserLoginRequest buildUserLoginRequest() {
        var request = new UserLoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("secret123");
        return request;
    }


}
