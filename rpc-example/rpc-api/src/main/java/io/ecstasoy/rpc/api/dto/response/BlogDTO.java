package io.ecstasoy.rpc.api.dto.response;

import lombok.Data;

/**
 * Blog DTO.
 */
@Data
public class BlogDTO {
  private String id;
  private String title;
  private String content;
  private String authorId;
  private String authorName;
  private Long createTime;
  private Long updateTime;
}
