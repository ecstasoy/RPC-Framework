package org.example.rpc.api.entity;

import lombok.Data;

@Data
public class BlogEntity {
  private String id;
  private String title;
  private String content;
  private String authorId;
  private String authorName;
  private Long createTime;
  private Long updateTime;
}
