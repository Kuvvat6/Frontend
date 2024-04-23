package ru.finance.my.view.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.User;
import ru.finance.my.listeners.ChangePasswordButtonListener;
import ru.finance.my.listeners.ChangeUserDateButtonListener;
import ru.finance.my.listeners.DeleteProfileButtonListener;
import ru.finance.my.listeners.ExitButtonListener;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.AppLayoutBasic;

import java.lang.reflect.Type;

@Route("/profile")
public class UserView extends AppLayoutBasic implements BeforeEnterObserver {

  private static final RestTemplate restTemplate = new RestTemplate();

  public UserView() {
    super(SelectedTab.PROFILE);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    User user;
    try {
      user = getUser();
      setContent(getContentComponent(user));
    } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
      beforeEnterEvent.rerouteTo(LoginView.class);
    }
  }

  private Component getContentComponent(User user) {
    VerticalLayout layout = new VerticalLayout();

    H3 userName = new H3("Name: " + user.getName());
    userName.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    Button changeUserDataButton = new Button("Edit data", e -> {
      UserDialog dialog = new UserDialog(user);
      dialog.open();
    });
    changeUserDataButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    Button changePasswordButton = new Button("Change password", e -> {
      ChangePasswordDialog dialog = new ChangePasswordDialog(user);
      dialog.open();
    });
    changePasswordButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    Button exitButton = new Button("Exit", new ExitButtonListener(restTemplate));
    exitButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    exitButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    Button deleteProfileButton = new Button("Delete profile", new DeleteProfileButtonListener(restTemplate));
    deleteProfileButton.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    deleteProfileButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

    layout.add(userName, changeUserDataButton, changePasswordButton, exitButton, deleteProfileButton);

    layout.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return layout;
  }

  private static class UserDialog extends Dialog {

    private final TextField nameTextField;

    private final TextField loginTextField;

    private final EmailField emailField;

    public UserDialog(User user) {
      setHeaderTitle("Edit your data");

      nameTextField = new TextField("Name");
      loginTextField = new TextField("Login");
      emailField = new EmailField("Email");

      nameTextField.setValue(user.getName());
      loginTextField.setValue(user.getLogin());
      emailField.setValue(user.getEmail());

      Button changeButton = new Button("Change");
      changeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      changeButton.addClickListener(new ChangeUserDateButtonListener(
          restTemplate,
          nameTextField,
          loginTextField,
          emailField,
          user
      ));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      nameTextField.addValueChangeListener(e -> changeButton.setEnabled(
          !nameTextField.getValue().isEmpty() &&
              !loginTextField.getValue().isEmpty() &&
              !emailField.getValue().isEmpty()
      ));
      loginTextField.addValueChangeListener(e -> changeButton.setEnabled(
          !nameTextField.getValue().isEmpty() &&
              !loginTextField.getValue().isEmpty() &&
              !emailField.getValue().isEmpty()
      ));
      emailField.addValueChangeListener(e -> changeButton.setEnabled(
          !nameTextField.getValue().isEmpty() &&
              !loginTextField.getValue().isEmpty() &&
              !emailField.getValue().isEmpty()
      ));

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(nameTextField, loginTextField, emailField);

      add(verticalLayout);

      getFooter().add(changeButton, cancelButton);
    }
  }

  private static class ChangePasswordDialog extends Dialog {

    private final PasswordField newPasswordField;

    public ChangePasswordDialog(User user) {
      newPasswordField = new PasswordField("New password");

      Button changeButton = new Button("Change");
      changeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      changeButton.setEnabled(false);
      changeButton.addClickListener(new ChangePasswordButtonListener(restTemplate, newPasswordField, user));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      newPasswordField.addValueChangeListener(e -> changeButton.setEnabled(!newPasswordField.getValue().isEmpty()));

      VerticalLayout layout = new VerticalLayout(newPasswordField);

      add(layout);

      getFooter().add(changeButton, cancelButton);
    }
  }

  private User getUser() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    String url = AppConfiguration.SERVER_HOST + "/user";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<User>() {}.getType();
    return gson.fromJson(response.getBody(), listType);
  }
}
