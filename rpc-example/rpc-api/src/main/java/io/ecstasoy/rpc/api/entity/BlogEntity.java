package io.ecstasoy.rpc.api.entity;

import lombok.Data;

/**
 * Blog entity.
 */
@Data
public class BlogEntity {
  private String id;
  private String title;
  private String content;
  private String authorId;
  private Long createTime;
  private Long updateTime;
}
