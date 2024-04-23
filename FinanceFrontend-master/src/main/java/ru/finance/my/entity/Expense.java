package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class Expense {

  private Long id;

  private Long sum;

  private Account account;

  private String date;

  private ExpenseCategory expenseCategory;

  public String getExpenseCategoryName() {
    return expenseCategory.getName();
  }

  public String getAccountName() {
    return account.getName();
  }
}
