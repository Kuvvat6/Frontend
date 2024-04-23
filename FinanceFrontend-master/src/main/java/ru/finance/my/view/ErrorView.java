package ru.finance.my.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/error")
public class ErrorView extends AppLayout {

  public ErrorView() {
    VerticalLayout verticalLayout = new VerticalLayout();
    H1 message = new H1("404. Not Found");
    message.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto")
        .set("font-size", "72pt");
    verticalLayout.add(message);
    setContent(verticalLayout);
  }
}
