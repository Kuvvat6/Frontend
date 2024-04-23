package ru.finance.my.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
public class Account {

  private Long id;

  private String name;

  private Long sum;

  private Long userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Account account = (Account) o;
    return Objects.equals(id, account.id) && Objects.equals(name, account.name) && Objects.equals(sum, account.sum) && Objects.equals(userId, account.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, sum, userId);
  }
}
