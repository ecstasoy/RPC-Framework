package io.ecstasoy.rpc.user.convert;

import io.ecstasoy.rpc.api.dto.request.CreateUserDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateUserDTO;
import io.ecstasoy.rpc.api.dto.response.UserDTO;
import io.ecstasoy.rpc.api.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

  public UserDTO toUserDTO(UserEntity entity) {
    UserDTO dto = new UserDTO();
    dto.setId(entity.getId());
    dto.setUsername(entity.getUsername());
    dto.setEmail(entity.getEmail());
    dto.setCreateTime(entity.getCreateTime());
    dto.setUpdateTime(entity.getUpdateTime());
    return dto;
  }

  public UserEntity toUserEntity(CreateUserDTO dto) {
    UserEntity entity = new UserEntity();
    entity.setUsername(dto.getUsername());
    entity.setPassword(dto.getPassword());
    entity.setEmail(dto.getEmail());
    entity.setCreateTime(System.currentTimeMillis());
    entity.setUpdateTime(System.currentTimeMillis());
    return entity;
  }

  public void updateEntity(UpdateUserDTO dto, UserEntity entity) {
    if (dto.getUsername() != null) {
      entity.setUsername(dto.getUsername());
    }
    if (dto.getPassword() != null) {
      entity.setPassword(dto.getPassword());
    }
    if (dto.getEmail() != null) {
      entity.setEmail(dto.getEmail());
    }
    entity.setUpdateTime(System.currentTimeMillis());
  }
}
