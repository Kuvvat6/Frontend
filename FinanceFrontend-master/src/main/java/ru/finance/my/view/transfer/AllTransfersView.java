package ru.finance.my.view.transfer;

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
import ru.finance.my.entity.Account;
import ru.finance.my.entity.Transfer;
import ru.finance.my.listeners.TransferDialogDeleteButtonListener;
import ru.finance.my.listeners.TransferDialogSaveButtonListener;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.AppLayoutBasic;
import ru.finance.my.view.ErrorView;
import ru.finance.my.view.user.LoginView;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;

@Route("/transfers/:time")
public class AllTransfersView extends AppLayoutBasic implements BeforeEnterObserver {

  private static final RestTemplate restTemplate = new RestTemplate();

  private String time;

  private Integer currentDay;

  private Integer currentMonth;

  private Integer currentYear;

  private String endDate;

  public static final String DAY = "day";
  public static final String WEEK = "week";
  public static final String MONTH = "month";
  public static final String YEAR = "year";
  public static final String PERIOD = "period";

  public AllTransfersView() {
    super(SelectedTab.TRANSFERS);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    Optional<String> optionalTime = beforeEnterEvent.getRouteParameters().get("time");
    Map<String, List<String>> parameters = beforeEnterEvent.getLocation().getQueryParameters().getParameters();
    if (
        optionalTime.isPresent() && (optionalTime.get().equals(DAY) || optionalTime.get().equals(WEEK) || optionalTime.get().equals(MONTH) || optionalTime.get().equals(YEAR) || optionalTime.get().equals(PERIOD))
        && !parameters.isEmpty() && parameters.containsKey("current-day") && parameters.containsKey("current-month") && parameters.containsKey("current-year")
    ) {
      currentDay = Integer.valueOf(parameters.get("current-day").get(0));
      currentMonth = Integer.valueOf(parameters.get("current-month").get(0));
      currentYear = Integer.valueOf(parameters.get("current-year").get(0));
      time = optionalTime.get();

      if (time.equals(PERIOD)) {
        endDate = beforeEnterEvent.getLocation().getQueryParameters().getParameters().get("end-date").get(0);
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
    if (accountList.size() < 2) {
      Dialog dialog = new Dialog();
      dialog.add(new Span("You must have at least 2 accounts. Create a new account to add transfers"));
      dialog.open();
      return;
    }

    Div div = new Div();

    Tabs dateTabs = getDateTabs();
    Component currentPeriodComponent = getCurrentPeriodComponent(getCurrentPeriod());
    TransferDialog editTransferDialog = new TransferDialog("Edit transfer", TransferDialog.Type.UPDATE, accountList);
    TransferDialog createTransferDialog = new TransferDialog("Create transfer", TransferDialog.Type.CREATE, accountList);
    Grid<Transfer> transferGrid = getTransferGrid();
    TransferContextMenu transferContextMenu = new TransferContextMenu(transferGrid, editTransferDialog, createTransferDialog);

    div.add(dateTabs, currentPeriodComponent, transferGrid, transferContextMenu);

    setContent(div);
  }

  private Grid<Transfer> getTransferGrid() {
    Grid<Transfer> transferGrid = new Grid<>(Transfer.class, false);
    transferGrid.addColumn(Transfer::getSum).setHeader("Sum").setSortable(true);
    transferGrid.addColumn(Transfer::getFromAccountName).setHeader("From");
    transferGrid.addColumn(Transfer::getToAccountName).setHeader("To");
    transferGrid.addColumn(Transfer::getDate).setHeader("Date").setSortable(true);

    transferGrid.setAllRowsVisible(true);
    transferGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    transferGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    transferGrid.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    transferGrid.setItems(getTransferList());

    return transferGrid;
  }

  private Tabs getDateTabs() {
    Tabs tabs = new Tabs();
    Tab dayTab = new Tab(getTimeRouterLink("Day"));
    Tab weekTab = new Tab(getTimeRouterLink("Week"));
    Tab monthTab = new Tab(getTimeRouterLink("Month"));
    Tab yearTab = new Tab(getTimeRouterLink("Year"));
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

    RouterLink confirmLink = new RouterLink(AllTransfersView.class, new RouteParameters(new RouteParam("time", PERIOD)));
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

      RouterLink previous = new RouterLink(AllTransfersView.class, new RouteParameters(new RouteParam("time", time)));
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

      RouterLink next = new RouterLink(AllTransfersView.class, new RouteParameters(new RouteParam("time", time)));
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

  private RouterLink getTimeRouterLink(String time) {
    RouterLink main = new RouterLink();
    main.setText(time);
    main.setRoute(AllTransfersView.class, new RouteParameters(new RouteParam("time", time.toLowerCase())));
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
      case MONTH -> currentDateString = Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
      case YEAR -> currentDateString = String.valueOf(currentYear);
      case PERIOD -> currentDateString += " - " + endDate;
    }
    return currentDateString;
  }

  private static class TransferContextMenu extends GridContextMenu<Transfer> {
    public TransferContextMenu(Grid<Transfer> target, TransferDialog editTransferDialog, TransferDialog createTransferDialog) {
      super(target);
      addItem("Create", e -> createTransferDialog.open());
      addItem("Edit", e -> {
        Transfer item = e.getItem().get();
        editTransferDialog.transfer.setId(item.getId());
        editTransferDialog.sumNumberField.setValue(Double.valueOf(item.getSum()));
        editTransferDialog.datePicker.setValue(LocalDate.parse(item.getDate()));
        editTransferDialog.fromAccountSelect.setValue(item.getFromAccount());
        editTransferDialog.toAccountSelect.setValue(item.getToAccount());
        editTransferDialog.open();
      });
      addItem("Delete", new TransferDialogDeleteButtonListener(restTemplate));
    }
  }

  private static class TransferDialog extends Dialog {

    private final NumberField sumNumberField;

    private final Select<Account> toAccountSelect;

    private final Select<Account> fromAccountSelect;

    private final DatePicker datePicker;

    private final Transfer transfer = Transfer.builder().build();

    enum Type {
      CREATE, UPDATE
    }

    public TransferDialog(String title, Type type, List<Account> accountList) {
      setHeaderTitle(title);

      toAccountSelect = new Select<>();
      fromAccountSelect = new Select<>();

      toAccountSelect.setItems(accountList);
      toAccountSelect.setItemLabelGenerator(Account::getName);
      toAccountSelect.setPlaceholder("To");

      fromAccountSelect.setItems(accountList);
      fromAccountSelect.setItemLabelGenerator(Account::getName);
      fromAccountSelect.setPlaceholder("From");

      datePicker = new DatePicker();
      datePicker.setPlaceholder("Date");

      sumNumberField = new NumberField();
      sumNumberField.setPlaceholder("Sum");

      if (type.equals(Type.CREATE)) {
        toAccountSelect.setValue(accountList.get(0));
        fromAccountSelect.setValue(accountList.get(1));
        sumNumberField.setValue(0.0);
        datePicker.setValue(LocalDate.now());
      }

      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.add(fromAccountSelect, toAccountSelect, datePicker, sumNumberField);

      add(verticalLayout);

      Button saveButton = new Button("Save");
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.addClickListener(new TransferDialogSaveButtonListener(
          sumNumberField,
          toAccountSelect,
          fromAccountSelect,
          datePicker,
          restTemplate,
          type.equals(Type.CREATE),
          transfer
      ));

      Button cancelButton = new Button("Cancel", e -> close());
      cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

      fromAccountSelect.addValueChangeListener(e -> {
        toAccountSelect.setItemEnabledProvider(item -> !item.equals(fromAccountSelect.getValue()));
        saveButton.setEnabled(
            sumNumberField.getValue() != null &&
                toAccountSelect.getValue() != null &&
                fromAccountSelect.getValue() != null &&
                datePicker.getValue() != null
        );
      });
      toAccountSelect.addValueChangeListener(e -> {
        fromAccountSelect.setItemEnabledProvider(item -> !item.equals(toAccountSelect.getValue()));
        saveButton.setEnabled(
            sumNumberField.getValue() != null &&
                toAccountSelect.getValue() != null &&
                fromAccountSelect.getValue() != null &&
                datePicker.getValue() != null
        );
      });

      getFooter().add(saveButton);
      getFooter().add(cancelButton);
    }
  }

  private List<Transfer> getTransferList() {
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
    String url = AppConfiguration.SERVER_HOST + "/transfers?before-date=" + beforeDate + "&after-date=" + afterDate;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Transfer>>() {
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
    Type listType = new TypeToken<List<Account>>() {}.getType();
    return gson.fromJson(response.getBody(), listType);
  }
}
