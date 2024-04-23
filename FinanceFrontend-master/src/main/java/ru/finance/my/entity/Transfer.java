package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class Transfer {

  private Long id;

  private Account fromAccount;

  private Account toAccount;

  private Long sum;

  private String date;

  public String getFromAccountName() {
    return fromAccount.getName();
  }

  public String getToAccountName() {
    return toAccount.getName();
  }
}
