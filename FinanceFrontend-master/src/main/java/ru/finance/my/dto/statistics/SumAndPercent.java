package ru.finance.my.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SumAndPercent {
  private Long sum;

  private Long percent;
}