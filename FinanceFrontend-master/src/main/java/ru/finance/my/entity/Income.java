package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class Income {

  private Long id;

  private Long sum;

  private Account account;

  private String date;

  private IncomeCategory incomeCategory;

  public String getIncomeCategoryName() {
    return incomeCategory.getName();
  }

  public String getAccountName() {
    return account.getName();
  }
}
