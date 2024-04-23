package ru.finance.my.listeners;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class DeleteProfileButtonListener implements ComponentEventListener<ClickEvent<Button>> {

  private final RestTemplate restTemplate;

  public DeleteProfileButtonListener(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/user";
      restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
    } catch (HttpClientErrorException.Forbidden | NullPointerException ignored) {}
    buttonClickEvent.getSource().getUI().get().navigate(LoginView.class);
  }
}