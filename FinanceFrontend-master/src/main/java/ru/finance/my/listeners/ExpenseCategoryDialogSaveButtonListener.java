package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.ExpenseCategory;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class ExpenseCategoryDialogSaveButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final TextField nameTextField;

  private final ExpenseCategory expenseCategory;

  private final boolean createMode;

  public ExpenseCategoryDialogSaveButtonListener(
      TextField nameTextField,
      RestTemplate restTemplate,
      ExpenseCategory expenseCategory,
      boolean createMode
  ) {
    this.nameTextField = nameTextField;
    this.restTemplate = restTemplate;
    this.expenseCategory = expenseCategory;
    this.createMode = createMode;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    String name = nameTextField.getValue();

    ExpenseCategory expenseCategoryRequestBody = ExpenseCategory.builder()
        .name(name)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<ExpenseCategory> httpEntity = new HttpEntity<>(expenseCategoryRequestBody, httpHeaders);

      if (createMode) {
        String url = AppConfiguration.SERVER_HOST + "/expense-categories";
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
      } else {
        String url = AppConfiguration.SERVER_HOST + "/expense-categories/" + expenseCategory.getId();
        restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      }

      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
    ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
  }
}