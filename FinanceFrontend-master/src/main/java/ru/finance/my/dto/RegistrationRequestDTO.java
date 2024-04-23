package ru.finance.my.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequestDTO {

  private String login;

  private String password;

  private String confirmOfPassword;

  private String name;

  private String email;
}
