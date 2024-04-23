package ru.finance.my.view;

import com.vaadin.flow.router.*;

import javax.servlet.http.HttpServletResponse;

public class CustomNotFoundTarget extends RouteNotFoundError {

  @Override
  public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
    event.rerouteTo(ErrorView.class);
    return HttpServletResponse.SC_NOT_FOUND;
  }
}
