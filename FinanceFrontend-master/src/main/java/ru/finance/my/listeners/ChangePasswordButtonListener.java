package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.User;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class ChangePasswordButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final PasswordField passwordField;

  private final User user;

  public ChangePasswordButtonListener(
      RestTemplate restTemplate,
      PasswordField passwordField,
      User user
  ) {
    this.restTemplate = restTemplate;
    this.passwordField = passwordField;
    this.user = user;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    String password = passwordField.getValue();

    User userRequestBody = User.builder()
        .password(password)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<User> httpEntity = new HttpEntity<>(userRequestBody, httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/user";
      restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
    ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
  }
}
