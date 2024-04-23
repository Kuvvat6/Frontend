package ru.finance.my.dto.statistics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.finance.my.entity.IncomeCategory;

@Getter
@Setter
@Builder
public class IncomeCategoryStatistics {

  private IncomeCategory incomeCategory;

  private SumAndPercent sumAndPercent;

  public String getIncomeCategoryName() {
    return incomeCategory.getName();
  }

  public Long getSum() {
    return sumAndPercent.getSum();
  }

  public Long getPercent() {
    return sumAndPercent.getPercent();
  }
}
