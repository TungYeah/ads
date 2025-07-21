package vn.minhtung.ads.controller;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import vn.minhtung.ads.domain.Role;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.request.LoginDTO;
import vn.minhtung.ads.domain.response.Role.RoleDTO;
import vn.minhtung.ads.domain.response.Role.RoleDTO.PermissionDTO;
import vn.minhtung.ads.domain.response.login.ResLoginDTO;
import vn.minhtung.ads.domain.response.user.CreateUserDTO;
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

    private final PasswordEncoder passwordEncoder;



    @Value("${minhtung.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpriation;

    public AuthController(AuthenticationManagerBuilder authenticationMangerBuilder,
            SecutiryUtil secutiryUtil,
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.authenticationMangerBuilder = authenticationMangerBuilder;
        this.secutiryUtil = secutiryUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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
            RoleDTO role = new RoleDTO();
            if (currentUser.getRole() != null) {
                Role currentRole = currentUser.getRole();

                List<PermissionDTO> permissionDTOs = currentRole.getPermissions() != null
                        ? currentRole.getPermissions().stream()
                                .map(p -> new PermissionDTO(p.getId(), p.getName()))
                                .collect(Collectors.toList())
                        : Collections.emptyList();

                role = new RoleDTO(currentRole.getId(), currentRole.getName(), permissionDTOs);
            }

            res.setUser(new ResLoginDTO.UserLogin(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getName(),
                    role));
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

    @PostMapping("/auth/register")
    public ResponseEntity<CreateUserDTO> createUser(@Valid @RequestBody User user) throws IdInvalidException {
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User postUser = this.userService.handleUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserDTO(postUser));
    }


    @GetMapping("/auth/account")
    @ApiMessage("account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() throws IdInvalidException {
        String email = SecutiryUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Access token không hợp lệ"));

        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("Không tìm thấy người dùng");
        }

        RoleDTO role = null;
        if (currentUser.getRole() != null) {
            Role currentRole = currentUser.getRole();
            List<PermissionDTO> permissionDTOs = currentRole.getPermissions() != null
                    ? currentRole.getPermissions().stream()
                            .map(p -> new PermissionDTO(p.getId(), p.getName()))
                            .collect(Collectors.toList())
                    : Collections.emptyList();
            role = new RoleDTO(currentRole.getId(), currentRole.getName(), permissionDTOs);
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                role);

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
        RoleDTO roleDTO = new RoleDTO();
        if (currentUser.getRole() != null) {
            Role role = currentUser.getRole();

            List<PermissionDTO> permissionDTOs = role.getPermissions() != null
                    ? role.getPermissions().stream()
                            .map(p -> new PermissionDTO(p.getId(), p.getName()))
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            roleDTO = new RoleDTO(role.getId(), role.getName(), permissionDTOs);
        }
        ResLoginDTO res = new ResLoginDTO();
        res.setUser(
                new ResLoginDTO.UserLogin(currentUser.getId(), currentUser.getEmail(), currentUser.getName(), roleDTO));
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
