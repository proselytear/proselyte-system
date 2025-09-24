package net.proselyte.api.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.api.service.TokenService;
import net.proselyte.api.service.UserService;
import net.proselyte.individual.dto.IndividualWriteDto;
import net.proselyte.individual.dto.TokenRefreshRequest;
import net.proselyte.individual.dto.TokenResponse;
import net.proselyte.individual.dto.UserInfoResponse;
import net.proselyte.individual.dto.UserLoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/auth")
public class AuthRestControllerV1 {

    private final UserService userService;
    private final TokenService tokenService;

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getMe() {
        return userService.getUserInfo().map(ResponseEntity::ok);
    }

    @PostMapping(value = "/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> body) {
        return body.flatMap(tokenService::login).map(ResponseEntity::ok);
    }

    @PostMapping(value = "/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody Mono<TokenRefreshRequest> body) {
        return body.flatMap(tokenService::refreshToken).map(ResponseEntity::ok);
    }

    @PostMapping(value = "/registration")
    public Mono<ResponseEntity<TokenResponse>> registration(@Valid @RequestBody Mono<IndividualWriteDto> body) {
        return body.flatMap(userService::register)
                .map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t));
    }
}
