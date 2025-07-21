package vn.minhtung.ads.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.user.CreateUserDTO;
import vn.minhtung.ads.domain.response.user.GetUserByIdDTO;
import vn.minhtung.ads.domain.response.user.UpdateUserDTO;
import vn.minhtung.ads.service.UserService;
import vn.minhtung.ads.util.anotation.ApiMessage;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<CreateUserDTO> createUser(@Valid @RequestBody User user) {
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User postUser = this.userService.handleUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserDTO(postUser));
    }

    @GetMapping("/users")
    @ApiMessage("Get All Users")
    public ResponseEntity<ResultPageinationDTO> getAllUser(
            @Filter Specification<User> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUsers(spec, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<GetUserByIdDTO> getUserById(@PathVariable long id) throws IdInvalidException {
        User getUserById = this.userService.getUserById(id);
        if (getUserById == null) {
            throw new IdInvalidException("Khong tim thay id nguoi dung" + id);
        }
        User user = this.userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserByIdDTO(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UpdateUserDTO> updateUserById(@PathVariable("id") long id, @RequestBody User user)
            throws IdInvalidException {
        User currentUser = this.userService.getUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("Ko ton tai id" + id);
        }
        User result = this.userService.updateUser(id, user);
        return ResponseEntity.ok(this.userService.convertUpdateUserDTO(result));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") long id) throws IdInvalidException {
        User curretUser = this.userService.getUserById(id);
        if (curretUser == null) {
            throw new IdInvalidException("User khong ton tai Id" + id);
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(null);
    }
}
