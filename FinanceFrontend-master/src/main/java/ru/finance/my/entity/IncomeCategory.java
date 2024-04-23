package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
public class IncomeCategory {

  private Long id;

  private String name;

  private Long userId;

  private Boolean accessToDelete;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IncomeCategory that = (IncomeCategory) o;
    return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(userId, that.userId) && Objects.equals(accessToDelete, that.accessToDelete);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, userId, accessToDelete);
  }
}
