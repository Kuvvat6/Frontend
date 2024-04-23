package ru.finance.my.view.category;

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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.entity.*;
import ru.finance.my.listeners.ExpenseCategoryDialogDeleteButtonListener;
import ru.finance.my.listeners.ExpenseCategoryDialogSaveButtonListener;
import ru.finance.my.listeners.IncomeCategoryDialogDeleteButtonListener;
import ru.finance.my.listeners.IncomeCategoryDialogSaveButtonListener;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.AppLayoutBasic;
import ru.finance.my.view.ErrorView;
import ru.finance.my.view.user.LoginView;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Route("/categories/:type")
public class AllCategoriesView extends AppLayoutBasic implements BeforeEnterObserver {

  private static final RestTemplate restTemplate = new RestTemplate();

  private String type;

  public static final String EXPENSES = "expenses";
  public static final String INCOMES = "incomes";

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    Optional<String> optionalType = beforeEnterEvent.getRouteParameters().get("type");
    if (optionalType.isPresent() && (optionalType.get().equals(EXPENSES) || optionalType.get().equals(INCOMES))) {
      type = optionalType.get();
      setContent(getContentComponent(beforeEnterEvent));
    } else {
      beforeEnterEvent.rerouteTo(ErrorView.class);
    }
  }

  public AllCategoriesView() {
    super(SelectedTab.CATEGORIES);
  }

  private Component getContentComponent(BeforeEnterEvent beforeEnterEvent) {
    Div div = new Div();

    Component createCategoryButtonDiv = getCreateCategoryButtonDiv();
    div.add(getExpenseAndIncomeTabs(), createCategoryButtonDiv);

    if (type.equals(EXPENSES)) {
      List<ExpenseCategory> expenseCategoryList;
      try {
        expenseCategoryList = getExpenseCategoryList();
        for (ExpenseCategory expenseCategory : expenseCategoryList) {
          if (!expenseCategory.getAccessToDelete()) {
            expenseCategoryList.remove(expenseCategory);
            break;
          }
        }
      } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
        beforeEnterEvent.rerouteTo(LoginView.class);
        return null;
      }
      ExpenseCategoryDialog createExpenseCategoryDialog = new ExpenseCategoryDialog("Create expense category", ExpenseCategoryDialog.Type.CREATE);
      ((Button)createCategoryButtonDiv.getChildren().toList().get(0)).addClickListener(e -> createExpenseCategoryDialog.open());
      if (!expenseCategoryList.isEmpty()) {
        Grid<ExpenseCategory> expenseCategoryGrid = getExpenseCategoryGrid(expenseCategoryList);
        ExpenseCategoryDialog editExpenseCategoryDialog = new ExpenseCategoryDialog("Edit expense category", ExpenseCategoryDialog.Type.UPDATE);
        ExpenseCategoryContextMenu expenseCategoryContextMenu = new ExpenseCategoryContextMenu(expenseCategoryGrid, editExpenseCategoryDialog, createExpenseCategoryDialog);
        div.add(expenseCategoryGrid, expenseCategoryContextMenu);
      }
    } else {
      List<IncomeCategory> incomeCategoryList;
      try {
        incomeCategoryList = getIncomeCategoryList();
        for (IncomeCategory incomeCategory : incomeCategoryList) {
          if (!incomeCategory.getAccessToDelete()) {
            incomeCategoryList.remove(incomeCategory);
            break;
          }
        }
      } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
        beforeEnterEvent.rerouteTo(LoginView.class);
        return null;
      }
      IncomeCategoryDialog createIncomeCategoryDialog = new IncomeCategoryDialog("Create income category", IncomeCategoryDialog.Type.CREATE);
      ((Button)createCategoryButtonDiv.getChildren().toList().get(0)).addClickListener(e -> createIncomeCategoryDialog.open());
      if (!incomeCategoryList.isEmpty()) {
        Grid<IncomeCategory> incomeCategoryGrid = getIncomeCategoryGrid(incomeCategoryList);
        IncomeCategoryDialog editIncomeCategoryDialog = new IncomeCategoryDialog("Edit income category", IncomeCategoryDialog.Type.UPDATE);
        IncomeCategoryContextMenu incomeCategoryContextMenu = new IncomeCategoryContextMenu(incomeCategoryGrid, editIncomeCategoryDialog, createIncomeCategoryDialog);
        div.add(incomeCategoryGrid, incomeCategoryContextMenu);
      }
    }

    return div;
  }

  private VerticalLayout getCreateCategoryButtonDiv() {
    VerticalLayout divButton = new VerticalLayout();

    Button button = new Button(VaadinIcon.PLUS.create());
    Span span = new Span("Create");

    button.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    span.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    divButton.add(button, span);

    divButton.setSpacing(false);
    divButton.getStyle()
        .set("width", "100px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return divButton;
  }

  private Grid<ExpenseCategory> getExpenseCategoryGrid(List<ExpenseCategory> expenseCategoryList) {
    Grid<ExpenseCategory> expenseCategoryGrid = new Grid<>(ExpenseCategory.class, false);

    expenseCategoryGrid.addColumn(ExpenseCategory::getName).setHeader("Name").setTextAlign(ColumnTextAlign.CENTER);

    expenseCategoryGrid.setAllRowsVisible(true);
    expenseCategoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    expenseCategoryGrid.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto")
        .set("width", "250px");

    expenseCategoryGrid.setItems(expenseCategoryList);

    return expenseCategoryGrid;
  }

  private Grid<IncomeCategory> getIncomeCategoryGrid(List<IncomeCategory> incomeCategoryList) {
    Grid<IncomeCategory> incomeCategoryGrid = new Grid<>(IncomeCategory.class, false);

    incomeCategoryGrid.addColumn(IncomeCategory::getName).setHeader("Name").setTextAlign(ColumnTextAlign.CENTER);

    incomeCategoryGrid.setAllRowsVisible(true);
    incomeCategoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    incomeCategoryGrid.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto")
        .set("width", "250px");

    incomeCategoryGrid.setItems(incomeCategoryList);

    return incomeCategoryGrid;
  }

  private Tabs getExpenseAndIncomeTabs() {
    Tabs tabs = new Tabs();
    Tab expenseTab = new Tab(getAllCategoriesExpensesRouterLink());
    Tab incomeTab = new Tab(getAllCategoriesIncomesRouterLink());

    expenseTab.getStyle()
        .set("margin-left", "auto");
    incomeTab.getStyle()
        .set("margin-right", "auto");

    tabs.add(expenseTab, incomeTab);

    if (type.equals(EXPENSES)) {
      tabs.setSelectedTab(expenseTab);
    } else {
      tabs.setSelectedTab(incomeTab);
    }

    tabs.setWidth("500px");
    tabs.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return tabs;
  }

  private RouterLink getAllCategoriesExpensesRouterLink() {
    RouterLink mainExpenses = new RouterLink();
    mainExpenses.setText("Expenses");
    mainExpenses.setRoute(AllCategoriesView.class, new RouteParameters(new RouteParam("type", EXPENSES)));
    return mainExpenses;
  }

  private RouterLink getAllCategoriesIncomesRouterLink() {
    RouterLink mainIncomes = new RouterLink();
    mainIncomes.setText("Incomes");
    mainIncomes.setRoute(AllCategoriesView.class, new RouteParameters(new RouteParam("type", INCOMES)));
    return mainIncomes;
  }

  private static class ExpenseCategoryContextMenu extends GridContextMenu<ExpenseCategory> {
    public ExpenseCategoryContextMenu(Grid<ExpenseCategory> target, ExpenseCategoryDialog editExpenseCategoryDialog, ExpenseCategoryDialog createExpenseCategoryDialog) {
      super(target);
      addItem("Create", e -> createExpenseCategoryDialog.open());
      addItem("Edit", e -> {
        ExpenseCategory item = e.getItem().get();
        editExpenseCategoryDialog.expenseCategory.setId(item.getId());
        editExpenseCategoryDialog.nameTextField.setValue(item.getName());
        editExpenseCategoryDialog.open();
      });
      addItem("Delete", new ExpenseCategoryDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class IncomeCategoryContextMenu extends GridContextMenu<IncomeCategory> {
    public IncomeCategoryContextMenu(Grid<IncomeCategory> target, IncomeCategoryDialog editIncomeCategoryDialog, IncomeCategoryDialog createIncomeCategoryDialog) {
      super(target);
      addItem("Create", e -> createIncomeCategoryDialog.open());
      addItem("Edit", e -> {
        IncomeCategory item = e.getItem().get();
        editIncomeCategoryDialog.incomeCategory.setId(item.getId());
        editIncomeCategoryDialog.nameTextField.setValue(item.getName());
        editIncomeCategoryDialog.open();
      });
      addItem("Delete", new IncomeCategoryDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class ExpenseCategoryDialog extends Dialog {

    private final TextField nameTextField;

    private final ExpenseCategory expenseCategory = ExpenseCategory.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public ExpenseCategoryDialog(String title, Type type) {
      setHeaderTitle(title);
      nameTextField = new TextField();
      nameTextField.setPlaceholder("Name");

      if (type.equals(Type.CREATE)) {
        nameTextField.setValue("Category name");
      }

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(nameTextField);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(new ExpenseCategoryDialogSaveButtonListener(
          nameTextField,
          restTemplate,
          expenseCategory,
          type.equals(Type.CREATE)
      ));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      nameTextField.addValueChangeListener(e -> saveButton.setEnabled(!nameTextField.getValue().isEmpty()));

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private static class IncomeCategoryDialog extends Dialog {

    private final TextField nameTextField;

    private final IncomeCategory incomeCategory = IncomeCategory.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public IncomeCategoryDialog(String title, Type type) {
      setHeaderTitle(title);
      nameTextField = new TextField();
      nameTextField.setPlaceholder("Name");

      if (type.equals(Type.CREATE)) {
        nameTextField.setValue("Category name");
      }

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(nameTextField);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(new IncomeCategoryDialogSaveButtonListener(
          nameTextField,
          restTemplate,
          incomeCategory,
          type.equals(Type.CREATE)
      ));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      nameTextField.addValueChangeListener(e -> saveButton.setEnabled(!nameTextField.getValue().isEmpty()));

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private List<ExpenseCategory> getExpenseCategoryList() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    String url = AppConfiguration.SERVER_HOST + "/expense-categories";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<ExpenseCategory>>() {
    }.getType();
    return gson.fromJson(response.getBody(), listType);
  }

  private List<IncomeCategory> getIncomeCategoryList() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    String url = AppConfiguration.SERVER_HOST + "/income-categories";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<IncomeCategory>>() {
    }.getType();
    return gson.fromJson(response.getBody(), listType);
  }
}
