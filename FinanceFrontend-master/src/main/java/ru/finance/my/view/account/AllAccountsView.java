package ru.finance.my.view.account;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
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
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.Account;
import ru.finance.my.listeners.AccountDialogDeleteButtonListener;
import ru.finance.my.listeners.AccountDialogSaveButtonListener;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.AppLayoutBasic;
import ru.finance.my.view.user.LoginView;

import java.lang.reflect.Type;
import java.util.List;

@Route("/accounts")
public class AllAccountsView extends AppLayoutBasic implements BeforeEnterObserver {

  private static final RestTemplate restTemplate = new RestTemplate();

  public AllAccountsView() {
    super(SelectedTab.ACCOUNTS);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    List<Account> accountList;
    try {
      accountList = getAccountList();
    } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
      beforeEnterEvent.rerouteTo(LoginView.class);
      return;
    }

    setContent(getContent(accountList));
  }

  private Component getContent(List<Account> accountList) {
    Grid<Account> accountGrid = getAccountGrid(accountList);
    AccountDialog editAccountDialog = new AccountDialog("Edit account", AccountDialog.Type.UPDATE);
    AccountDialog createAccountDialog = new AccountDialog("Create account", AccountDialog.Type.CREATE);
    AccountContextMenu accountContextMenu = new AccountContextMenu(accountGrid, editAccountDialog, createAccountDialog);

    H3 title = new H3("Accounts");
    Div divTitle = new Div();
    divTitle.add(title);
    divTitle.getStyle()
        .set("text-align", "center")
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    Div div = new Div();

    div.add(divTitle, accountGrid, accountContextMenu);

    return div;
  }

  private Grid<Account> getAccountGrid(List<Account> accountList) {
    Grid<Account> accountGrid = new Grid<>(Account.class, false);
    accountGrid.addColumn(Account::getName).setHeader("Name")
        .setSortable(true).setResizable(true);
    accountGrid.addColumn(Account::getSum).setHeader("Sum")
        .setSortable(true).setTextAlign(ColumnTextAlign.CENTER);

    accountGrid.setAllRowsVisible(true);
    accountGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    accountGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    accountGrid.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    accountGrid.setItems(accountList);

    return accountGrid;
  }

  private static class AccountContextMenu extends GridContextMenu<Account> {
    public AccountContextMenu(Grid<Account> target, AccountDialog editAccountDialog, AccountDialog createAccountDialog) {
      super(target);
      addItem("Create", e -> createAccountDialog.open());
      addItem("Edit", e -> {
        Account item = e.getItem().get();
        editAccountDialog.account.setId(item.getId());
        editAccountDialog.nameTextField.setValue(item.getName());
        editAccountDialog.sumNumberField.setValue(Double.valueOf(item.getSum()));
        editAccountDialog.open();
      });
      addItem("Delete", new AccountDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class AccountDialog extends Dialog {

    private final TextField nameTextField;

    private final NumberField sumNumberField;

    private final Account account = Account.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public AccountDialog(String title, Type type) {
      setHeaderTitle(title);
      nameTextField = new TextField();
      nameTextField.setPlaceholder("Name");
      sumNumberField = new NumberField();
      sumNumberField.setPlaceholder("Sum");

      if (type.equals(Type.CREATE)) {
        nameTextField.setValue("Account name");
        sumNumberField.setValue(0.0);
      }

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(nameTextField, sumNumberField);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(new AccountDialogSaveButtonListener(
          nameTextField,
          sumNumberField,
          restTemplate,
          account,
          type.equals(Type.CREATE)
      ));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      nameTextField.addValueChangeListener(e -> saveButton.setEnabled(!nameTextField.getValue().isEmpty() && sumNumberField.getValue() != null));
      sumNumberField.addValueChangeListener(e -> saveButton.setEnabled(!nameTextField.getValue().isEmpty() && sumNumberField.getValue() != null));

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private List<Account> getAccountList() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    String url = AppConfiguration.SERVER_HOST + "/accounts";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Account>>() {}.getType();
    return gson.fromJson(response.getBody(), listType);
  }
}
