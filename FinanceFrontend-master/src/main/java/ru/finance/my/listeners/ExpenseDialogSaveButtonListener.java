package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.Account;
import ru.finance.my.entity.Expense;
import ru.finance.my.entity.ExpenseCategory;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

import java.time.LocalDate;

public class ExpenseDialogSaveButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final NumberField sumNumberField;
  private final Select<Account> accountSelect;
  private final Select<ExpenseCategory> expenseCategorySelect;
  private final DatePicker datePicker;

  private final boolean createMode;

  private final Expense expense;

  public ExpenseDialogSaveButtonListener(
      NumberField sumNumberField,
      Select<Account> accountSelect,
      Select<ExpenseCategory> expenseCategorySelect,
      DatePicker datePicker,
      RestTemplate restTemplate,
      boolean createMode,
      Expense expense
  ) {
    this.sumNumberField = sumNumberField;
    this.accountSelect = accountSelect;
    this.expenseCategorySelect = expenseCategorySelect;
    this.datePicker = datePicker;
    this.restTemplate = restTemplate;
    this.createMode = createMode;
    this.expense = expense;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    Long sum = sumNumberField.getValue().longValue();
    Account account = accountSelect.getValue();
    ExpenseCategory expenseCategory = expenseCategorySelect.getValue();
    LocalDate date = datePicker.getValue();

    Expense expenseRequestBody = Expense.builder()
        .expenseCategory(expenseCategory)
        .account(account)
        .date(date.toString())
        .sum(sum)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<Expense> httpEntity = new HttpEntity<>(expenseRequestBody, httpHeaders);

      if (createMode) {
        String url = AppConfiguration.SERVER_HOST + "/expenses";
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
      } else {
        String url = AppConfiguration.SERVER_HOST + "/expenses/" + expense.getId();
        restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      }

      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
    ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
  }
}
