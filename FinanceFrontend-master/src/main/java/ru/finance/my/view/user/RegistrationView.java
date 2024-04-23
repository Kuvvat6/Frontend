package ru.finance.my.view.user;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.listeners.RegistrationButtonListener;

@Route("/registration")
public class RegistrationView extends AppLayout {

  public RegistrationView() {
    VerticalLayout layout = new VerticalLayout();

    H3 registration = new H3("Registration");
    registration.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    TextField email = new TextField("Email");
    email.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    PasswordField password = new PasswordField("Password");
    password.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    PasswordField confirmPassword = new PasswordField("Confirm password");
    confirmPassword.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    TextField login = new TextField("Login");
    login.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    TextField name = new TextField("Name");
    name.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    RestTemplate restTemplate = new RestTemplate();
    Button okButton = new Button("Ok", new RegistrationButtonListener(
        restTemplate,
        email,
        password,
        confirmPassword,
        login,
        name
    ));
    okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    okButton.setEnabled(false);
    okButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    layout.add(registration, email, password, confirmPassword, login, name, okButton);

    email.addValueChangeListener(e -> okButton.setEnabled(
        !email.getValue().isEmpty() &&
            !login.getValue().isEmpty() &&
            !password.getValue().isEmpty() &&
            !confirmPassword.getValue().isEmpty() &&
            !name.getValue().isEmpty() &&
            password.getValue().equals(confirmPassword.getValue())
    ));
    login.addValueChangeListener(e -> okButton.setEnabled(
        !email.getValue().isEmpty() &&
            !login.getValue().isEmpty() &&
            !password.getValue().isEmpty() &&
            !confirmPassword.getValue().isEmpty() &&
            !name.getValue().isEmpty() &&
            password.getValue().equals(confirmPassword.getValue())
    ));
    password.addValueChangeListener(e -> okButton.setEnabled(
        !email.getValue().isEmpty() &&
            !login.getValue().isEmpty() &&
            !password.getValue().isEmpty() &&
            !confirmPassword.getValue().isEmpty() &&
            !name.getValue().isEmpty() &&
            password.getValue().equals(confirmPassword.getValue())
    ));
    confirmPassword.addValueChangeListener(e -> okButton.setEnabled(
        !email.getValue().isEmpty() &&
            !login.getValue().isEmpty() &&
            !password.getValue().isEmpty() &&
            !confirmPassword.getValue().isEmpty() &&
            !name.getValue().isEmpty() &&
            password.getValue().equals(confirmPassword.getValue())
    ));
    name.addValueChangeListener(e -> okButton.setEnabled(
        !email.getValue().isEmpty() &&
            !login.getValue().isEmpty() &&
            !password.getValue().isEmpty() &&
            !confirmPassword.getValue().isEmpty() &&
            !name.getValue().isEmpty() &&
            password.getValue().equals(confirmPassword.getValue())
    ));

    layout.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    setContent(layout);
  }
}
