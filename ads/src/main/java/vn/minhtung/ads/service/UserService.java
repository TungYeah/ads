package vn.minhtung.ads.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.minhtung.ads.domain.User;
import vn.minhtung.ads.domain.dto.*;
import vn.minhtung.ads.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exits");
        }
        return this.userRepository.save(user);
    }

    public ResultPageinationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPageinationDTO rs = new ResultPageinationDTO();
        Meta mt = new Meta();

        mt.setPage(pageUser.getNumber() + 1);
        mt.setPageSize(pageUser.getSize());
        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());
        rs.setMeta(mt);


        List<GetUserByIdDTO> listUser = pageUser.getContent()
                .stream().map(user -> {
                    List<GetAdByIdDTO> ads = user.getAdsList()
                            .stream()
                            .map(ad -> new GetAdByIdDTO(
                                    ad.getId(),
                                    ad.getTitle(),
                                    ad.getDescription(),
                                    ad.getImageUrl(),
                                    ad.getTargetUrl(),
                                    ad.getStartDate(),
                                    ad.getEndDate()))
                            .collect(Collectors.toList());

                    return new GetUserByIdDTO(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getAge(),
                            user.getGender(),
                            user.getAddress(),
                            user.getCreatedAt(),
                            user.getUpdatedAt(),
                            ads);
                }).collect(Collectors.toList());

        rs.setResult(listUser);
        return rs;
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id).orElseThrow();
    }

    public User updateUser(long id, User updatedUser) {
        User currentUser = this.getUserById(updatedUser.getId());
        if (currentUser != null) {
            currentUser.setAddress(updatedUser.getAddress());
            currentUser.setGender(updatedUser.getGender());
            currentUser.setAge(updatedUser.getAge());
            currentUser.setName(updatedUser.getName());
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found");
        }
        this.userRepository.deleteById(id);
    }

    public User handleGetUserByUsername(String name) {
        return this.userRepository.findByEmail(name);
    }

    public CreateUserDTO convertUserDTO(User user) {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setId(user.getId());
        createUserDTO.setName(user.getName());
        createUserDTO.setEmail(user.getEmail());
        createUserDTO.setAge(user.getAge());
        createUserDTO.setCreatedAt(user.getCreatedAt());
        createUserDTO.setGender(user.getGender());
        createUserDTO.setAddress(user.getAddress());
        return createUserDTO;
    }

    public GetUserByIdDTO convertUserByIdDTO(User user) {
        GetUserByIdDTO getUserByIdDTO = new GetUserByIdDTO();
        getUserByIdDTO.setId(user.getId());
        getUserByIdDTO.setName(user.getName());
        getUserByIdDTO.setEmail(user.getEmail());
        getUserByIdDTO.setAge(user.getAge());
        getUserByIdDTO.setCreatedAt(user.getCreatedAt());
        getUserByIdDTO.setGender(user.getGender());
        getUserByIdDTO.setAddress(user.getAddress());
        getUserByIdDTO.setUpdateAt(user.getUpdatedAt());
        List<GetAdByIdDTO> adDTOs = user.getAdsList().stream()
                .map(ad -> new GetAdByIdDTO(
                        ad.getId(),
                        ad.getTitle(),
                        ad.getDescription(),
                        ad.getImageUrl(),
                        ad.getTargetUrl(),
                        ad.getStartDate(),
                        ad.getEndDate()))
                .collect(Collectors.toList());
        getUserByIdDTO.setAds(adDTOs);
        return getUserByIdDTO;
    }

    public UpdateUserDTO convertUpdateUserDTO(User user) {
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(user.getId());
        updateUserDTO.setName(user.getName());
        updateUserDTO.setAge(user.getAge());
        updateUserDTO.setGender(user.getGender());
        updateUserDTO.setAddress(user.getAddress());
        updateUserDTO.setUpdateAt(user.getUpdatedAt());
        return updateUserDTO;
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
}
