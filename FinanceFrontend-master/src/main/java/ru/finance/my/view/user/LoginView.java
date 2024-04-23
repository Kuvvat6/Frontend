package ru.finance.my.view.user;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.listeners.LoginOkButtonListener;

@Route("/login")
public class LoginView extends AppLayout {

  private boolean loginNotNull;

  private boolean passwordNotNull;

  public LoginView() {
    VerticalLayout layout = new VerticalLayout();

    H3 title = new H3("Login");
    title.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    TextField login = new TextField("Login");
    login.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    PasswordField password = new PasswordField("Password");
    password.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    Button okButton = new Button("Ok");
    okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    okButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    okButton.setEnabled(false);

    RouterLink link = new RouterLink("Registration", RegistrationView.class);
    link.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    RestTemplate restTemplate = new RestTemplate();
    okButton.addClickListener(new LoginOkButtonListener(login, password, restTemplate, this));

    layout.add(title, login, password, okButton, link);

    login.addValueChangeListener(e -> {
      if (!login.getValue().isEmpty()) {
        loginNotNull = true;
        if (passwordNotNull) {
          okButton.setEnabled(true);
        }
      } else {
        loginNotNull = false;
        okButton.setEnabled(false);
      }
    });
    password.addValueChangeListener(e -> {
      if (!password.getValue().isEmpty()) {
        passwordNotNull = true;
        if (loginNotNull) {
          okButton.setEnabled(true);
        }
      } else {
        passwordNotNull = false;
        okButton.setEnabled(false);
      }
    });

    layout.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    setContent(layout);
  }
}
