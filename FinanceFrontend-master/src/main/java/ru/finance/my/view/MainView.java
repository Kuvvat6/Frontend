package ru.finance.my.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.NumberField;
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
import ru.finance.my.listeners.ExpenseDialogDeleteButtonListener;
import ru.finance.my.listeners.ExpenseDialogSaveButtonListener;
import ru.finance.my.listeners.IncomeDialogDeleteButtonListener;
import ru.finance.my.listeners.IncomeDialogSaveButtonListener;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.user.LoginView;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;

@Route("/:type/:time")
public class MainView extends AppLayoutBasic implements BeforeEnterObserver {

  private String type;

  private String time;

  private Integer currentDay;

  private Integer currentMonth;

  private Integer currentYear;

  private String endDate;

  public static final String EXPENSES = "expenses";
  public static final String INCOMES = "incomes";
  public static final String DAY = "day";
  public static final String WEEK = "week";
  public static final String MONTH = "month";
  public static final String YEAR = "year";
  public static final String PERIOD = "period";

  private static final RestTemplate restTemplate = new RestTemplate();

  public MainView() {
    super(SelectedTab.MAIN);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    Optional<String> optionalType = beforeEnterEvent.getRouteParameters().get("type");
    Optional<String> optionalTime = beforeEnterEvent.getRouteParameters().get("time");
    Map<String, List<String>> parameters = beforeEnterEvent.getLocation().getQueryParameters().getParameters();
    if (
        optionalType.isPresent() && (optionalType.get().equals(EXPENSES) || optionalType.get().equals(INCOMES)) &&
            optionalTime.isPresent() && (optionalTime.get().equals(DAY) || optionalTime.get().equals(WEEK) || optionalTime.get().equals(MONTH) || optionalTime.get().equals(YEAR) || optionalTime.get().equals(PERIOD))
            && !parameters.isEmpty() && parameters.containsKey("current-day") && parameters.containsKey("current-month") && parameters.containsKey("current-year")
    ) {
      currentDay = Integer.valueOf(parameters.get("current-day").get(0));
      currentMonth = Integer.valueOf(parameters.get("current-month").get(0));
      currentYear = Integer.valueOf(parameters.get("current-year").get(0));
      type = optionalType.get();
      time = optionalTime.get();

      if (time.equals(PERIOD)) {
        endDate = parameters.get("end-date").get(0);
      }

      createContent(beforeEnterEvent);
    } else {
      beforeEnterEvent.rerouteTo(ErrorView.class);
    }
  }

  private void createContent(BeforeEnterEvent beforeEnterEvent) {
    List<Account> accountList;
    try {
      accountList = getAccountList();
    } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
      beforeEnterEvent.rerouteTo(LoginView.class);
      return;
    }
    if (accountList.isEmpty()) {
      Dialog dialog = new Dialog();
      dialog.add(new Span("You don`t have accounts. Create a new account to add expenses or incomes"));
      dialog.open();
      return;
    }

    Div div = new Div();

    Tabs expenseAndIncomeTabs = getExpenseAndIncomeTabs();
    Tabs dateTabs = getDateTabs();
    Component currentPeriodComponent = getCurrentPeriodComponent(getCurrentPeriod());

    div.add(expenseAndIncomeTabs, dateTabs, currentPeriodComponent);

    if (type.equals(EXPENSES)) {
      List<Expense> expenses;
      try {
        expenses = getExpenseValue();
      } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
        beforeEnterEvent.rerouteTo(LoginView.class);
        return;
      }
      ExpenseDialog createExpenseDialog = new ExpenseDialog("Create expense", ExpenseDialog.Type.CREATE, accountList);
      if (expenses != null && !expenses.isEmpty()) {
        Grid<Expense> expenseGrid = getExpenseGrid(expenses);
        ExpenseDialog editExpenseDialog = new ExpenseDialog("Edit expense", ExpenseDialog.Type.UPDATE, accountList);
        ExpenseContextMenu expenseContextMenu = new ExpenseContextMenu(expenseGrid, editExpenseDialog, createExpenseDialog);
        div.add(expenseGrid, expenseContextMenu);
      } else {
        Button createButton = new Button("Create", e -> createExpenseDialog.open());
        Div divButton = new Div();
        divButton.add(createButton);
        divButton.getStyle()
            .set("width", "100px")
            .set("margin-left", "auto")
            .set("margin-right", "auto");
        div.add(divButton);
      }
    } else {
      List<Income> incomes;
      try {
        incomes = getIncomeValue();
      } catch (HttpClientErrorException.Forbidden | NullPointerException exception) {
        beforeEnterEvent.rerouteTo(LoginView.class);
        return;
      }
      IncomeDialog createIncomeDialog = new IncomeDialog("Create income", IncomeDialog.Type.CREATE, accountList);
      if (incomes != null && !incomes.isEmpty()) {
        Grid<Income> incomeGrid = getIncomeGrid(incomes);
        IncomeDialog editIncomeDialog = new IncomeDialog("Edit income", IncomeDialog.Type.UPDATE, accountList);
        IncomeContextMenu incomeContextMenu = new IncomeContextMenu(incomeGrid, editIncomeDialog, createIncomeDialog);
        div.add(incomeGrid, incomeContextMenu);
      } else {
        Button createButton = new Button("Create", e -> createIncomeDialog.open());
        Div divButton = new Div();
        divButton.add(createButton);
        divButton.getStyle()
            .set("width", "100px")
            .set("margin-left", "auto")
            .set("margin-right", "auto");
        div.add(divButton);
      }
    }

    setContent(div);
  }

  private Grid<Expense> getExpenseGrid(List<Expense> expenseList) {
    Grid<Expense> expenseGrid = new Grid<>(Expense.class, false);

    expenseGrid.addColumn(Expense::getSum).setHeader("Sum").setSortable(true)
        .setResizable(true);
    expenseGrid.addColumn(Expense::getExpenseCategoryName).setHeader("Category").setSortable(true)
        .setResizable(true);
    expenseGrid.addColumn(Expense::getDate).setHeader("Date").setSortable(true)
        .setResizable(true);
    expenseGrid.addColumn(Expense::getAccountName).setHeader("Account").setSortable(true);

    expenseGrid.setAllRowsVisible(true);
    expenseGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    expenseGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    expenseGrid.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    expenseGrid.setItems(expenseList);

    return expenseGrid;
  }

  private Grid<Income> getIncomeGrid(List<Income> expenseList) {
    Grid<Income> incomeGrid = new Grid<>(Income.class, false);

    incomeGrid.addColumn(Income::getSum).setHeader("Sum").setSortable(true)
        .setResizable(true);
    incomeGrid.addColumn(Income::getIncomeCategoryName).setHeader("Category").setSortable(true)
        .setResizable(true);
    incomeGrid.addColumn(Income::getDate).setHeader("Date").setSortable(true)
        .setResizable(true);
    incomeGrid.addColumn(Income::getAccountName).setHeader("Account").setSortable(true);

    incomeGrid.setAllRowsVisible(true);
    incomeGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    incomeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    incomeGrid.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    incomeGrid.setItems(expenseList);

    return incomeGrid;
  }

  private Tabs getExpenseAndIncomeTabs() {
    Tabs tabs = new Tabs();
    Tab expenseTab = new Tab(getMainExpensesRouterLink());
    Tab incomeTab = new Tab(getMainIncomesRouterLink());

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

  private Tabs getDateTabs() {
    Tabs tabs = new Tabs();
    Tab dayTab = new Tab(getMainTimeRouterLink("Day"));
    Tab weekTab = new Tab(getMainTimeRouterLink("Week"));
    Tab monthTab = new Tab(getMainTimeRouterLink("Month"));
    Tab yearTab = new Tab(getMainTimeRouterLink("Year"));
    Tab periodTab = new Tab(getPeriodButton());

    dayTab.getStyle()
        .set("margin-left", "auto");
    periodTab.getStyle()
        .set("margin-right", "auto");

    tabs.add(dayTab, weekTab, monthTab, yearTab, periodTab);

    switch (time) {
      case DAY -> tabs.setSelectedTab(dayTab);
      case WEEK -> tabs.setSelectedTab(weekTab);
      case MONTH -> tabs.setSelectedTab(monthTab);
      case YEAR -> tabs.setSelectedTab(yearTab);
      case PERIOD -> tabs.setSelectedTab(periodTab);
    }

    tabs.setWidth("500px");
    tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
    tabs.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return tabs;
  }

  private Component getPeriodButton() {
    Div div = new Div();

    Button periodButton = new Button("Period");

    Dialog dialog = new Dialog();

    dialog.setHeaderTitle("Period");

    RouterLink confirmLink = new RouterLink(MainView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", PERIOD)));
    confirmLink.setText("Confirm");

    HorizontalLayout dateHorizontalLayout = new HorizontalLayout();
    DatePicker startDate = new DatePicker("Start date");
    DatePicker endDate = new DatePicker("End date");

    startDate.setValue(LocalDate.now().minusDays(6));
    endDate.setValue(LocalDate.now());

    endDate.setMin(startDate.getValue());
    endDate.setMax(LocalDate.now());
    startDate.setMax(endDate.getValue());

    startDate.addValueChangeListener(e -> {
      endDate.setMin(e.getValue());
      confirmLink.setQueryParameters(getQueryParametersForPeriod(startDate.getValue(), endDate.getValue()));
    });
    endDate.addValueChangeListener(e -> {
      startDate.setMax(e.getValue());
      confirmLink.setQueryParameters(getQueryParametersForPeriod(startDate.getValue(), endDate.getValue()));
    });
    dateHorizontalLayout.add(startDate, endDate);

    confirmLink.setQueryParameters(getQueryParametersForPeriod(startDate.getValue(), endDate.getValue()));

    Button cancelButton = new Button("Cancel");
    cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    cancelButton.addClickListener(e -> dialog.close());

    HorizontalLayout buttonsHorizontalLayout = new HorizontalLayout();
    buttonsHorizontalLayout.add(confirmLink, cancelButton);

    VerticalLayout verticalLayout = new VerticalLayout();
    verticalLayout.add(dateHorizontalLayout, buttonsHorizontalLayout);

    dialog.add(verticalLayout);

    div.add(dialog, periodButton);

    periodButton.addClickListener(e -> dialog.open());
    periodButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    return div;
  }

  private QueryParameters getQueryParametersForPeriod(LocalDate startDate, LocalDate endDate) {
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("current-day", List.of(String.valueOf(startDate.get(ChronoField.DAY_OF_MONTH))));
    parameters.put("current-month", List.of(String.valueOf(startDate.get(ChronoField.MONTH_OF_YEAR))));
    parameters.put("current-year", List.of(String.valueOf(startDate.get(ChronoField.YEAR))));
    parameters.put("end-date", List.of(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    return new QueryParameters(parameters);
  }

  private Component getCurrentPeriodComponent(String currentPeriod) {
    HorizontalLayout horizontalLayout = new HorizontalLayout();

    Div divCurrentPeriod = new Div();

    divCurrentPeriod.setText(currentPeriod);
    divCurrentPeriod.getStyle()
        .set("text-align", "center");

    if (!time.equals(PERIOD)) {
      LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);

      RouterLink previous = new RouterLink(MainView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time)));
      Map<String, List<String>> parametersPrevious = new HashMap<>();
      LocalDate newDatePrevious = LocalDate.now();
      switch (time) {
        case DAY -> newDatePrevious = currentDate.minusDays(1);
        case WEEK -> newDatePrevious = currentDate.minusWeeks(1);
        case MONTH -> newDatePrevious = currentDate.minusMonths(1);
        case YEAR -> newDatePrevious = currentDate.minusYears(1);
      }
      parametersPrevious.put("current-day", List.of(String.valueOf(newDatePrevious.get(ChronoField.DAY_OF_MONTH))));
      parametersPrevious.put("current-month", List.of(String.valueOf(newDatePrevious.get(ChronoField.MONTH_OF_YEAR))));
      parametersPrevious.put("current-year", List.of(String.valueOf(newDatePrevious.get(ChronoField.YEAR))));
      previous.setQueryParameters(new QueryParameters(parametersPrevious));
      previous.add(new Icon(VaadinIcon.ANGLE_LEFT));

      RouterLink next = new RouterLink(MainView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time)));
      Map<String, List<String>> parametersNext = new HashMap<>();
      LocalDate newDateNext = LocalDate.now();
      switch (time) {
        case DAY -> newDateNext = currentDate.plusDays(1);
        case WEEK -> newDateNext = currentDate.plusWeeks(1);
        case MONTH -> newDateNext = currentDate.plusMonths(1);
        case YEAR -> newDateNext = currentDate.plusYears(1);
      }
      if (newDateNext.isAfter(LocalDate.now())) {
        next.setVisible(false);
      } else {
        parametersNext.put("current-day", List.of(String.valueOf(newDateNext.get(ChronoField.DAY_OF_MONTH))));
        parametersNext.put("current-month", List.of(String.valueOf(newDateNext.get(ChronoField.MONTH_OF_YEAR))));
        parametersNext.put("current-year", List.of(String.valueOf(newDateNext.get(ChronoField.YEAR))));
        next.setQueryParameters(new QueryParameters(parametersNext));
        next.add(new Icon(VaadinIcon.ANGLE_RIGHT));
      }

      previous.getStyle()
          .set("margin-right", "auto");
      next.getStyle()
          .set("margin-left", "auto");

      Div div = new Div();
      div.getStyle()
          .set("width", "24px")
          .set("margin-left", "auto");

      horizontalLayout.add(previous, divCurrentPeriod, (next.isVisible()) ? next : div);
    } else {
      divCurrentPeriod.getStyle()
          .set("margin-left", "auto")
          .set("margin-right", "auto");
      horizontalLayout.add(divCurrentPeriod);
    }

    horizontalLayout.setWidth("500px");
    horizontalLayout.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return horizontalLayout;
  }

  private RouterLink getMainExpensesRouterLink() {
    RouterLink mainExpenses = new RouterLink();
    mainExpenses.setText("Expenses");
    mainExpenses.setRoute(MainView.class, new RouteParameters(new RouteParam("type", MainView.EXPENSES), new RouteParam("time", time)));
    Map<String, List<String>> parameterExpenses = new HashMap<>();
    parameterExpenses.put("current-day", List.of(String.valueOf(currentDay)));
    parameterExpenses.put("current-month", List.of(String.valueOf(currentMonth)));
    parameterExpenses.put("current-year", List.of(String.valueOf(currentYear)));
    if (time.equals(PERIOD)) {
      parameterExpenses.put("end-date", List.of(endDate));
    }
    mainExpenses.setQueryParameters(new QueryParameters(parameterExpenses));
    return mainExpenses;
  }

  private RouterLink getMainIncomesRouterLink() {
    RouterLink mainIncomes = new RouterLink();
    mainIncomes.setText("Incomes");
    mainIncomes.setRoute(MainView.class, new RouteParameters(new RouteParam("type", MainView.INCOMES), new RouteParam("time", time)));
    Map<String, List<String>> parameterIncomes = new HashMap<>();
    parameterIncomes.put("current-day", List.of(String.valueOf(currentDay)));
    parameterIncomes.put("current-month", List.of(String.valueOf(currentMonth)));
    parameterIncomes.put("current-year", List.of(String.valueOf(currentYear)));
    if (time.equals(PERIOD)) {
      parameterIncomes.put("end-date", List.of(endDate));
    }
    mainIncomes.setQueryParameters(new QueryParameters(parameterIncomes));
    return mainIncomes;
  }

  private RouterLink getMainTimeRouterLink(String time) {
    RouterLink main = new RouterLink();
    main.setText(time);
    main.setRoute(MainView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time.toLowerCase())));
    Map<String, List<String>> parameter = new HashMap<>();
    parameter.put("current-day", List.of(String.valueOf(currentDay)));
    parameter.put("current-month", List.of(String.valueOf(currentMonth)));
    parameter.put("current-year", List.of(String.valueOf(currentYear)));
    main.setQueryParameters(new QueryParameters(parameter));
    return main;
  }

  private String getCurrentPeriod() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);
    String currentDateString = currentDate.format(formatter);
    switch (time) {
      case WEEK -> {
        LocalDate startDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_WEEK) - 1);
        LocalDate endDate = startDate.plusDays(6);
        currentDateString = startDate.format(formatter) + " - " + endDate.format(formatter);
      }
      case MONTH ->
          currentDateString = Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "(" + currentYear + ")";
      case YEAR -> currentDateString = String.valueOf(currentYear);
      case PERIOD -> currentDateString += " - " + endDate;
    }
    return currentDateString;
  }

  private static class ExpenseContextMenu extends GridContextMenu<Expense> {
    public ExpenseContextMenu(Grid<Expense> target, ExpenseDialog editExpenseDialog, ExpenseDialog createExpenseDialog) {
      super(target);
      addItem("Create", e -> createExpenseDialog.open());
      addItem("Edit", e -> {
        Expense item = e.getItem().get();
        editExpenseDialog.expense.setId(item.getId());
        editExpenseDialog.datePicker.setValue(LocalDate.parse(item.getDate()));
        editExpenseDialog.sumNumberField.setValue(Double.valueOf(item.getSum()));
        editExpenseDialog.accountSelect.setValue(item.getAccount());
        editExpenseDialog.expenseCategorySelect.setValue(item.getExpenseCategory());
        editExpenseDialog.open();
      });
      addItem("Delete", new ExpenseDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class IncomeContextMenu extends GridContextMenu<Income> {
    public IncomeContextMenu(Grid<Income> target, IncomeDialog editIncomeDialog, IncomeDialog createIncomeDialog) {
      super(target);
      addItem("Create", e -> createIncomeDialog.open());
      addItem("Edit", e -> {
        Income item = e.getItem().get();
        editIncomeDialog.income.setId(item.getId());
        editIncomeDialog.datePicker.setValue(LocalDate.parse(item.getDate()));
        editIncomeDialog.sumNumberField.setValue(Double.valueOf(item.getSum()));
        editIncomeDialog.accountSelect.setValue(item.getAccount());
        editIncomeDialog.incomeCategorySelect.setValue(item.getIncomeCategory());
        editIncomeDialog.open();
      });
      addItem("Delete", new IncomeDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class ExpenseDialog extends Dialog {
    private final NumberField sumNumberField;
    private final Select<Account> accountSelect;
    private final Select<ExpenseCategory> expenseCategorySelect;
    private final DatePicker datePicker;

    private boolean sumNumberFieldIsNull = false;

    private boolean accountSelectIsNull = false;

    private boolean expenseCategorySelectIsNull = false;

    private boolean datePickerIsNull = false;

    private final Expense expense = Expense.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public ExpenseDialog(String title, Type type, List<Account> accountList) {
      setHeaderTitle(title);
      sumNumberField = new NumberField();
      sumNumberField.setPlaceholder("Sum");
      accountSelect = new Select<>();
      accountSelect.setItems(accountList);
      accountSelect.setItemLabelGenerator(Account::getName);
      accountSelect.setPlaceholder("Account");
      expenseCategorySelect = new Select<>();
      List<ExpenseCategory> expenseCategoryList = getExpenseCategoryList();
      expenseCategorySelect.setItems(expenseCategoryList);
      expenseCategorySelect.setItemLabelGenerator(ExpenseCategory::getName);
      expenseCategorySelect.setPlaceholder("Expense category");
      datePicker = new DatePicker();
      datePicker.setPlaceholder("Date");

      if (type.equals(Type.CREATE)) {
        sumNumberField.setValue(1.0);
        accountSelect.setValue(accountList.get(0));
        expenseCategorySelect.setValue(expenseCategoryList.get(0));
        datePicker.setValue(LocalDate.now());
      }

      datePicker.setMax(LocalDate.now());

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(sumNumberField, accountSelect, expenseCategorySelect, datePicker);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(
          new ExpenseDialogSaveButtonListener(
              sumNumberField,
              accountSelect,
              expenseCategorySelect,
              datePicker,
              restTemplate,
              type.equals(Type.CREATE),
              expense
          )
      );
      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      sumNumberField.addValueChangeListener(e -> {
        if (sumNumberField.getValue() != null && sumNumberField.getValue() > 0) {
          sumNumberFieldIsNull = false;
          if (!accountSelectIsNull && !expenseCategorySelectIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          sumNumberFieldIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      accountSelect.addValueChangeListener(e -> {
        if (accountSelect.getValue() != null) {
          accountSelectIsNull = false;
          if (!sumNumberFieldIsNull && !expenseCategorySelectIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          accountSelectIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      expenseCategorySelect.addValueChangeListener(e -> {
        if (expenseCategorySelect.getValue() != null) {
          expenseCategorySelectIsNull = false;
          if (!accountSelectIsNull && !sumNumberFieldIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          expenseCategorySelectIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      datePicker.addValueChangeListener(e -> {
        if (datePicker.getValue() != null) {
          datePickerIsNull = false;
          if (!accountSelectIsNull && !expenseCategorySelectIsNull && !sumNumberFieldIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          datePickerIsNull = true;
          saveButton.setEnabled(false);
        }
      });

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private static class IncomeDialog extends Dialog {
    private final NumberField sumNumberField;
    private final Select<Account> accountSelect;
    private final Select<IncomeCategory> incomeCategorySelect;
    private final DatePicker datePicker;

    private boolean sumNumberFieldIsNull = false;

    private boolean accountSelectIsNull = false;

    private boolean incomeCategorySelectIsNull = false;

    private boolean datePickerIsNull = false;

    private final Income income = Income.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public IncomeDialog(String title, Type type, List<Account> accountList) {
      setHeaderTitle(title);
      sumNumberField = new NumberField();
      sumNumberField.setPlaceholder("Sum");
      accountSelect = new Select<>();
      accountSelect.setItems(accountList);
      accountSelect.setItemLabelGenerator(Account::getName);
      accountSelect.setPlaceholder("Account");
      incomeCategorySelect = new Select<>();
      List<IncomeCategory> incomeCategoryList = getIncomeCategoryList();
      incomeCategorySelect.setItems(incomeCategoryList);
      incomeCategorySelect.setItemLabelGenerator(IncomeCategory::getName);
      incomeCategorySelect.setPlaceholder("Income category");
      datePicker = new DatePicker();
      datePicker.setPlaceholder("Date");

      if (type.equals(Type.CREATE)) {
        sumNumberField.setValue(1.0);
        accountSelect.setValue(accountList.get(0));
        incomeCategorySelect.setValue(incomeCategoryList.get(0));
        datePicker.setValue(LocalDate.now());
      }

      datePicker.setMax(LocalDate.now());

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(sumNumberField, accountSelect, incomeCategorySelect, datePicker);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(
          new IncomeDialogSaveButtonListener(
              sumNumberField,
              accountSelect,
              incomeCategorySelect,
              datePicker,
              restTemplate,
              type.equals(Type.CREATE),
              income
          )
      );

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      sumNumberField.addValueChangeListener(e -> {
        if (sumNumberField.getValue() != null && sumNumberField.getValue() > 0) {
          sumNumberFieldIsNull = false;
          if (!accountSelectIsNull && !incomeCategorySelectIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          sumNumberFieldIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      accountSelect.addValueChangeListener(e -> {
        if (accountSelect.getValue() != null) {
          accountSelectIsNull = false;
          if (!sumNumberFieldIsNull && !incomeCategorySelectIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          accountSelectIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      incomeCategorySelect.addValueChangeListener(e -> {
        if (incomeCategorySelect.getValue() != null) {
          incomeCategorySelectIsNull = false;
          if (!accountSelectIsNull && !sumNumberFieldIsNull && !datePickerIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          incomeCategorySelectIsNull = true;
          saveButton.setEnabled(false);
        }
      });
      datePicker.addValueChangeListener(e -> {
        if (datePicker.getValue() != null) {
          datePickerIsNull = false;
          if (!accountSelectIsNull && !incomeCategorySelectIsNull && !sumNumberFieldIsNull) {
            saveButton.setEnabled(true);
          }
        } else {
          datePickerIsNull = true;
          saveButton.setEnabled(false);
        }
      });

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private List<Expense> getExpenseValue() throws HttpClientErrorException.Forbidden, NullPointerException {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    LocalDate beforeDate = LocalDate.now();
    LocalDate afterDate = LocalDate.now();
    LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);
    switch (time) {
      case DAY -> {
        beforeDate = currentDate;
        afterDate = currentDate;
      }
      case WEEK -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_WEEK) - 1);
        beforeDate = afterDate.plusDays(6);
      }
      case MONTH -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_MONTH) - 1);
        beforeDate = afterDate.plusDays(Month.of(currentMonth).length(Year.isLeap(currentYear)) - 1);
      }
      case YEAR -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_YEAR) - 1);
        beforeDate = afterDate.plusDays(Year.of(currentYear).length() - 1);
      }
      case PERIOD -> {
        afterDate = currentDate;
        beforeDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      }
    }
    String url = AppConfiguration.SERVER_HOST + "/expenses?before-date=" + beforeDate + "&after-date=" + afterDate;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Expense>>() {
    }.getType();
    return gson.fromJson(response.getBody(), listType);
  }

  private List<Income> getIncomeValue() throws HttpClientErrorException.Forbidden, NullPointerException {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    LocalDate beforeDate = LocalDate.now();
    LocalDate afterDate = LocalDate.now();
    LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);
    switch (time) {
      case DAY -> {
        beforeDate = currentDate;
        afterDate = currentDate;
      }
      case WEEK -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_WEEK) - 1);
        beforeDate = afterDate.plusDays(6);
      }
      case MONTH -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_MONTH) - 1);
        beforeDate = afterDate.plusDays(Month.of(currentMonth).length(Year.isLeap(currentYear)) - 1);
      }
      case YEAR -> {
        afterDate = currentDate.minusDays(currentDate.get(ChronoField.DAY_OF_YEAR) - 1);
        beforeDate = afterDate.plusDays(Year.of(currentYear).length() - 1);
      }
      case PERIOD -> {
        afterDate = currentDate;
        beforeDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      }
    }
    String url = AppConfiguration.SERVER_HOST + "/incomes?before-date=" + beforeDate + "&after-date=" + afterDate;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Income>>() {
    }.getType();
    return gson.fromJson(response.getBody(), listType);
  }

  private static List<Account> getAccountList() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/json");
    httpHeaders.set(HttpHeaders.COOKIE, "JSESSIONID=" + CookieUtils.getCookieByNameFromRequest("TOKEN", VaadinService.getCurrentRequest()).getValue() + "; Path=/; HttpOnly");
    HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
    String url = AppConfiguration.SERVER_HOST + "/accounts";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Account>>() {
    }.getType();
    return gson.fromJson(response.getBody(), listType);
  }

  private static List<ExpenseCategory> getExpenseCategoryList() {
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

  private static List<IncomeCategory> getIncomeCategoryList() {
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
