package ru.finance.my.listeners;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.Transfer;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class TransferDialogDeleteButtonListener implements ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<Transfer>> {

  private final RestTemplate restTemplate;

  public TransferDialogDeleteButtonListener(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void onComponentEvent(GridContextMenu.GridContextMenuItemClickEvent<Transfer> transferGridContextMenuItemClickEvent) {
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/transfers/" + transferGridContextMenuItemClickEvent.getItem().get().getId();
      restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);

      transferGridContextMenuItemClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      transferGridContextMenuItemClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
  }
}
