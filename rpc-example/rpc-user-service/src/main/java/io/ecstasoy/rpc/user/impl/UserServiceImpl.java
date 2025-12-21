package io.ecstasoy.rpc.user.impl;

import io.ecstasoy.rpc.common.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import io.ecstasoy.rpc.api.dto.request.CreateUserDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateUserDTO;
import io.ecstasoy.rpc.api.dto.response.UserDTO;
import io.ecstasoy.rpc.api.exception.InvalidUserInputException;
import io.ecstasoy.rpc.api.exception.user.DuplicateUserException;
import io.ecstasoy.rpc.api.exception.user.UserDeletionException;
import io.ecstasoy.rpc.api.exception.user.UserNotFoundException;
import io.ecstasoy.rpc.api.exception.user.UserUpdateException;
import io.ecstasoy.rpc.api.service.UserService;
import io.ecstasoy.rpc.user.convert.UserConverter;
import io.ecstasoy.rpc.api.entity.UserEntity;
import io.ecstasoy.rpc.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * User service implementation.
 *
 * <p>Provide user-related services.
 *
 * @author Kunhua Huang
 */
@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserConverter userConverter;

  /**
   * Constructor.
   *
   * @param userRepository user repository
   * @param userConverter user converter
   */
  public UserServiceImpl(UserRepository userRepository, UserConverter userConverter) {
    this.userRepository = userRepository;
    this.userConverter = userConverter;
  }

  @GET("/{id}")
  @Override
  public CompletableFuture<UserDTO> selectById(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      UserEntity userEntity = userRepository.findById(id);
      if (userEntity == null) {
        log.warn("User not found with id: {}", id);
        throw new UserNotFoundException("User not found, ID: " + id);
      }
      log.info("Select user by id: {}", id);
      return userConverter.toUserDTO(userEntity);
    });
  }

  @POST
  @Override
  public CompletableFuture<UserDTO> createUser(@Body CreateUserDTO createUserDTO) {
    return CompletableFuture.supplyAsync(() -> {
      validateCreateUserDTO(createUserDTO);
      String id = createUserDTO.getId() == null ? RandomStringUtils.randomAlphanumeric(6) : createUserDTO.getId();
      UserEntity userEntity = userConverter.toUserEntity(createUserDTO);
      userEntity.setId(id);
      userEntity.setCreateTime(System.currentTimeMillis());
      userEntity.setUpdateTime(System.currentTimeMillis());
      userRepository.save(userEntity);
      log.info("Created user: {}", createUserDTO);
      return userConverter.toUserDTO(userEntity);
    });
  }

  private void validateCreateUserDTO(CreateUserDTO dto) {
    if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
      throw new InvalidUserInputException("Password cannot be null or empty");
    }
    if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
      throw new InvalidUserInputException("Username cannot be null or empty");
    }
    if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
      throw new InvalidUserInputException("Email cannot be null or empty");
    }
    if (dto.getId() != null && userRepository.findById(dto.getId()) != null) {
      throw new DuplicateUserException("User with ID " + dto.getId() + " already exists");
    }
    if (userRepository.findByUsername(dto.getUsername()) != null) {
      throw new DuplicateUserException("Username " + dto.getUsername() + " already exists");
    }
    if (userRepository.findByEmail(dto.getEmail()) != null) {
      throw new DuplicateUserException("Email " + dto.getEmail() + " already exists");
    }
  }

  @PUT("/{id}")
  @Override
  public CompletableFuture<UserDTO> updateUserId(@Path("id") String id, @Body UpdateUserDTO updateUserDTO) {
    return CompletableFuture.supplyAsync(() -> {
      UserEntity existingUser = userRepository.findById(id);
      if (existingUser == null) {
        throw new UserUpdateException("User not found, ID: " + id);
      }

      userConverter.updateEntity(updateUserDTO, existingUser);
      userRepository.save(existingUser);
      log.info("Updated user: {}", existingUser);

      return userConverter.toUserDTO(existingUser);
    });
  }

  @DELETE("/{id}")
  @Override
  public CompletableFuture<Void> deleteUser(@Path("id") String id) {
    return CompletableFuture.runAsync(() -> {
      UserEntity removedUser = userRepository.findById(id);
      if (removedUser == null) {
        throw new UserDeletionException("User not found, ID: " + id);
      }
      userRepository.deleteById(id);
      log.info("Deleted user with id: {}", id);
    });
  }

  @POST("/batch")
  @Override
  public CompletableFuture<List<UserDTO>> createUsers(@Body List<CreateUserDTO> createUserDTOs) {
    return CompletableFuture.supplyAsync(() -> {
      Set<String> specifiedIds = createUserDTOs.stream()
          .filter(dto -> dto.getId() != null)
          .map(CreateUserDTO::getId)
          .collect(Collectors.toSet());

      if (specifiedIds.size() < createUserDTOs.stream()
          .filter(dto -> dto.getId() != null).count()) {
        throw new DuplicateUserException("Duplicate IDs are not allowed");
      }

      for (String id : specifiedIds) {
        if (userRepository.findById(id) != null) {
          throw new DuplicateUserException("User with id " + id + " already exists");
        }
      }

      List<UserDTO> createdUsers = new ArrayList<>();
      for (CreateUserDTO createUserDTO : createUserDTOs) {
        String id = createUserDTO.getId() == null
            ? RandomStringUtils.randomAlphanumeric(6) : createUserDTO.getId();
        validateCreateUserDTO(createUserDTO);
        UserEntity userEntity = userConverter.toUserEntity(createUserDTO);
        userEntity.setId(id);
        userRepository.save(userEntity);

        createdUsers.add(userConverter.toUserDTO(userEntity));
        log.info("Created user: {}", userEntity);
      }
      return createdUsers;
    });
  }

  @GET("/all")
  @Override
  public CompletableFuture<List<UserDTO>> selectAll() {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Select all users");
      List<UserEntity> userEntities = userRepository.findAll();
      return userEntities.stream()
          .map(userConverter::toUserDTO)
          .collect(Collectors.toList());
    });
  }

  @GET("/{id}/exists")
  @Override
  public CompletableFuture<Void> validateUser(@Path("id") String id, @Query("username") String username) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Check if user exists with id: {}", id);
      if (userRepository.findById(id) == null) {
        throw new UserNotFoundException("User not found, ID: " + id);
      }
      return null;
    });
  }
}
