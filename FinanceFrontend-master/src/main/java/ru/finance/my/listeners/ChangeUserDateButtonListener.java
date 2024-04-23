package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
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

public class ChangeUserDateButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  private final TextField nameTextField;

  private final TextField loginTextField;

  private final EmailField emailField;

  private final User user;

  public ChangeUserDateButtonListener(
      RestTemplate restTemplate,
      TextField nameTextField,
      TextField loginTextField,
      EmailField emailField,
      User user
  ) {
    this.restTemplate = restTemplate;
    this.nameTextField = nameTextField;
    this.loginTextField = loginTextField;
    this.emailField = emailField;
    this.user = user;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    String name = nameTextField.getValue();
    String login = loginTextField.getValue();
    String email = emailField.getValue();

    User userRequestBody = User.builder()
        .name(name)
        .login(login)
        .email(email)
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<User> httpEntity = new HttpEntity<>(userRequestBody, httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/user";
      restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

      ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
      buttonClickEvent.getSource().getUI().get().getPage().reload();
    } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
      ((Dialog)buttonClickEvent.getSource().getParent().get()).close();
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    } catch (HttpClientErrorException.BadRequest exception) {
      Dialog dialog = new Dialog();
      dialog.add(new Span("Email is in use by another user"));
      dialog.open();
    }
  }
}
