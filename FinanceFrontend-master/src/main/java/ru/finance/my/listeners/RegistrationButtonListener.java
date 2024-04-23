package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.dto.RegistrationRequestDTO;
import ru.finance.my.view.user.LoginView;

public class RegistrationButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;
  private final TextField email;
  private final PasswordField password;
  private final PasswordField confirmPassword;
  private final TextField login;
  private final TextField name;

  public RegistrationButtonListener(
      RestTemplate restTemplate,
      TextField email,
      PasswordField password,
      PasswordField confirmPassword,
      TextField login,
      TextField name
  ) {
    this.restTemplate = restTemplate;
    this.email = email;
    this.password = password;
    this.confirmPassword = confirmPassword;
    this.login = login;
    this.name = name;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    RegistrationRequestDTO registrationRequestDTO = RegistrationRequestDTO.builder()
        .login(login.getValue())
        .password(password.getValue())
        .confirmOfPassword(confirmPassword.getValue())
        .email(email.getValue())
        .name(name.getValue())
        .build();

    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      HttpEntity<RegistrationRequestDTO> httpEntity = new HttpEntity<>(registrationRequestDTO, httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/registration";
      restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
      buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
    } catch (RestClientException ignored) {
      Dialog errorDialog = new Dialog();

      errorDialog.add("Invalid login or password!");

      errorDialog.getFooter().add(new Button("Close", e1 -> errorDialog.close()));

      errorDialog.open();
    }
  }
}
