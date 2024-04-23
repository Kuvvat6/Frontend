package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.Account;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class AccountDialogSaveButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final TextField nameTextField;

  private final NumberField sumNumberField;

  private final Account account;

  private final boolean createMode;

  public AccountDialogSaveButtonListener(
      TextField nameTextField,
      NumberField sumNumberField,
      RestTemplate restTemplate,
      Account account,
      boolean createMode
  ) {
    this.nameTextField = nameTextField;
    this.sumNumberField = sumNumberField;
    this.restTemplate = restTemplate;
    this.account = account;
    this.createMode = createMode;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    String name = nameTextField.getValue();
    Long sum = sumNumberField.getValue().longValue();

    Account accountRequestBody = Account.builder()
        .name(name)
        .sum(sum)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<Account> httpEntity = new HttpEntity<>(accountRequestBody, httpHeaders);

      if (createMode) {
        String url = AppConfiguration.SERVER_HOST + "/accounts";
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
      } else {
        String url = AppConfiguration.SERVER_HOST + "/accounts/" + account.getId();
        restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
      }

      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
    ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
  }
}