package vn.minhtung.ads.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.request.LoginDTO;
import vn.minhtung.ads.domain.response.login.ResLoginDTO;
import vn.minhtung.ads.service.UserService;
import vn.minhtung.ads.util.SecutiryUtil;
import vn.minhtung.ads.util.anotation.ApiMessage;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationMangerBuilder;
    private final SecutiryUtil secutiryUtil;
    private final UserService userService;

    @Value("${minhtung.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpriation;

    public AuthController(AuthenticationManagerBuilder authenticationMangerBuilder,
            SecutiryUtil secutiryUtil,
            UserService userService) {
        this.authenticationMangerBuilder = authenticationMangerBuilder;
        this.secutiryUtil = secutiryUtil;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDto.getUsername(),
                loginDto.getPassword());

        Authentication authentication = authenticationMangerBuilder.getObject().authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = userService.handleGetUserByUsername(loginDto.getUsername());

        ResLoginDTO res = new ResLoginDTO();
        if (currentUser != null) {
            res.setUser(new ResLoginDTO.UserLogin(currentUser.getId(), currentUser.getEmail(), currentUser.getName()));
        }

        String access_token = secutiryUtil.createAccessToken(authentication.getName(), res.getUser());
        res.setAccessToken(access_token);

        String refresh_token = secutiryUtil.createRefreshToken(loginDto.getUsername(), res);
        userService.updateUserToken(refresh_token, loginDto.getUsername());

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpriation)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }

    @GetMapping("/auth/account")
    @ApiMessage("account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecutiryUtil.getCurrentUserLogin().orElse(" ");
        User currentUser = userService.handleGetUserByUsername(email);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        if (currentUser != null) {
            userLogin.setId(currentUser.getId());
            userLogin.setEmail(currentUser.getEmail());
            userLogin.setName(currentUser.getName());
        }

        return ResponseEntity.ok(new ResLoginDTO.UserGetAccount(userLogin));
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue("refresh_token") String refresh_token)
            throws IdInvalidException {

        Jwt jwt = secutiryUtil.checkValidRefreshToken(refresh_token);
        String email = jwt.getSubject();

        User currentUser = userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if (currentUser == null)
            throw new IdInvalidException("Ko hop le");

        ResLoginDTO res = new ResLoginDTO();
        res.setUser(new ResLoginDTO.UserLogin(currentUser.getId(), currentUser.getEmail(), currentUser.getName()));
        res.setAccessToken(secutiryUtil.createAccessToken(email, res.getUser()));

        String newRefresh = secutiryUtil.createRefreshToken(email, res);
        userService.updateUserToken(newRefresh, email);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", newRefresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpriation)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("dang xuat thanh cong")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecutiryUtil.getCurrentUserLogin().orElse(" ");
        if (email.equals(" ")) {
            throw new IdInvalidException("Access Token ko hop le");
        }

        userService.updateUserToken(null, email);

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
