package org.example.rpc.api.pojo;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

import java.io.Serializable;

/**
 * Blog POJO.
 */
@Data
@JSONType(typeName = "org.example.rpc.api.pojo.Blog")
public class Blog implements Serializable {

  private String id;

  private String title;

  private String content;

  private User author;

  /**
   * Constructor.
   */
  public Blog() {
  }

  /**
   * Constructor.
   *
   * @param id      blog ID
   * @param title   blog title
   * @param content blog content
   * @param author  blog author
   */
  public Blog(String id, String title, String content, User author) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.author = author;
  }
}
