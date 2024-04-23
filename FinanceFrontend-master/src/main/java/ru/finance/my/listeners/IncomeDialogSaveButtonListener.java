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
import ru.finance.my.entity.*;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

import java.time.LocalDate;

public class IncomeDialogSaveButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final NumberField sumNumberField;
  private final Select<Account> accountSelect;
  private final Select<IncomeCategory> incomeCategorySelect;
  private final DatePicker datePicker;

  private final boolean createMode;

  private final Income income;

  public IncomeDialogSaveButtonListener(
      NumberField sumNumberField,
      Select<Account> accountSelect,
      Select<IncomeCategory> incomeCategorySelect,
      DatePicker datePicker,
      RestTemplate restTemplate,
      boolean createMode,
      Income income
  ) {
    this.sumNumberField = sumNumberField;
    this.accountSelect = accountSelect;
    this.incomeCategorySelect = incomeCategorySelect;
    this.datePicker = datePicker;
    this.restTemplate = restTemplate;
    this.createMode = createMode;
    this.income = income;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    Long sum = sumNumberField.getValue().longValue();
    Account account = accountSelect.getValue();
    IncomeCategory incomeCategory = incomeCategorySelect.getValue();
    LocalDate date = datePicker.getValue();

    Income incomeRequestBody = Income.builder()
        .incomeCategory(incomeCategory)
        .account(account)
        .date(date.toString())
        .sum(sum)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<Income> httpEntity = new HttpEntity<>(incomeRequestBody, httpHeaders);

      if (createMode) {
        String url = AppConfiguration.SERVER_HOST + "/incomes";
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
      } else {
        String url = AppConfiguration.SERVER_HOST + "/incomes/" + income.getId();
        restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      }

      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
    ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
  }
}
