package ru.finance.my.dto.statistics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class StatisticsDTO {

  private Long expenseSum;

  private Long incomeSum;

  private Map<Long, ExpenseCategoryStatistics> percentOfSumOfExpenseCategory;

  private Map<Long, IncomeCategoryStatistics> percentOfSumOfIncomeCategory;
}
