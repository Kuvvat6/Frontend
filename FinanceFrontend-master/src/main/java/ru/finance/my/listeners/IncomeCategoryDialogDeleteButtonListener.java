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
import ru.finance.my.entity.IncomeCategory;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

public class IncomeCategoryDialogDeleteButtonListener  implements ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<IncomeCategory>> {

  private final RestTemplate restTemplate;

  public IncomeCategoryDialogDeleteButtonListener(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void onComponentEvent(GridContextMenu.GridContextMenuItemClickEvent<IncomeCategory> incomeCategoryGridContextMenuItemClickEvent) {
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
      HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

      String url = AppConfiguration.SERVER_HOST + "/income-categories/" + incomeCategoryGridContextMenuItemClickEvent.getItem().get().getId();
      restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);

      incomeCategoryGridContextMenuItemClickEvent.getSource().getUI().get().getPage().reload();
    } catch (RestClientException | NullPointerException exception) {
      incomeCategoryGridContextMenuItemClickEvent.getSource().getUI().get().navigate(LoginView.class);
    }
  }
}
