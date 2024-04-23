package ru.finance.my.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginRequestDTO {

  private String username;

  private String password;
}
