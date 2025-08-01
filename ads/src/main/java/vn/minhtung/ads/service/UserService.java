package vn.minhtung.ads.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Role;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;

import vn.minhtung.ads.domain.response.user.CreateUserDTO;
import vn.minhtung.ads.domain.response.user.GetUserByIdDTO;
import vn.minhtung.ads.domain.response.user.UpdateUserDTO;
import vn.minhtung.ads.mapper.UserMapper;
import vn.minhtung.ads.repository.UserRepository;
import vn.minhtung.ads.util.PaginationUtil;
import vn.minhtung.ads.util.errors.IdInvalidException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleService roleService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userMapper = userMapper;
    }

    public User handleUser(User user) throws IdInvalidException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IdInvalidException("Email already exists");
        }
        if (user.getRole() != null) {
            Role role = this.roleService.getById(user.getRole().getId());
            user.setRole(role != null ? role : null);
        }
        return this.userRepository.save(user);
    }

    public ResultPageinationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        List<GetUserByIdDTO> users = pageUser.getContent()
                .stream()
                .map(userMapper::toGetUserByIdDTO)
                .collect(Collectors.toList());
        return PaginationUtil.build(pageUser, users);
    }

    public GetUserByIdDTO getUserDTOById(long id) {
        User user = userRepository.findById(id).orElseThrow();
        return userMapper.toGetUserByIdDTO(user);
    }

    public User getUserById(long id) throws IdInvalidException {
        return userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User not found"));
    }

    @CacheEvict(value = "users", key = "#id")
    public UpdateUserDTO updateUser(long id, UpdateUserDTO updatedDto) throws IdInvalidException {
        User currentUser = this.getUserById(id);
        currentUser.setAddress(updatedDto.getAddress());
        currentUser.setGender(updatedDto.getGender());
        currentUser.setAge(updatedDto.getAge());
        currentUser.setName(updatedDto.getName());
        currentUser.setUpdatedAt(updatedDto.getUpdatedAt());
        currentUser.setUpdatedBy(updatedDto.getUpdatedBy());

        return userMapper.toUpdateUserDTO(this.userRepository.save(currentUser));
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found");
        }
        this.userRepository.deleteById(id);
    }

    public User handleGetUserByUsername(String name) {
        return this.userRepository.findByEmail(name);
    }

    public void updateUserToken(String token, String email) {
        User user = this.handleGetUserByUsername(email);
        if (user != null) {
            user.setRefreshToken(token);
            this.userRepository.save(user);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public CreateUserDTO convertUserDTO(User user) {
        return userMapper.toCreateUserDTO(user);
    }
}
