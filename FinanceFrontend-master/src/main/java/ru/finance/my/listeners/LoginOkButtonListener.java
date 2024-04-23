package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.dto.LoginRequestDTO;
import ru.finance.my.view.user.UserView;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.stream.Collectors;

public class LoginOkButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final TextField login;

  private final PasswordField password;

  private final RestTemplate restTemplate;

  private final Component parent;

  public LoginOkButtonListener(TextField login, PasswordField password, RestTemplate restTemplate, Component parent) {
    this.login = login;
    this.password = password;
    this.restTemplate = restTemplate;
    this.parent = parent;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
        .username(login.getValue())
        .password(password.getValue())
        .build();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    HttpEntity<LoginRequestDTO> httpEntity = new HttpEntity<>(loginRequestDTO, httpHeaders);
    try {
      ResponseEntity<String> response = restTemplate.exchange(AppConfiguration.SERVER_HOST + "/login", HttpMethod.POST, httpEntity, String.class);
      List<String> cookies = response.getHeaders().get("Set-Cookie");
      Cookie cookie = new Cookie("TOKEN", cookies.get(0).substring(11, 43));
      cookie.setPath("/");
      cookie.setHttpOnly(true);
      VaadinService.getCurrentResponse().addCookie(cookie);
      parent.getUI().get().navigate(UserView.class);
    } catch (Exception exception) {
      Dialog errorDialog = new Dialog();

      errorDialog.add("Invalid login or password!");

      errorDialog.getFooter().add(new Button("Close", e1 -> errorDialog.close()));

      errorDialog.open();
    }
  }
}
