package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class User {

  private Long id;

  private String login;

  private String password;

  private String name;

  private String email;
}
