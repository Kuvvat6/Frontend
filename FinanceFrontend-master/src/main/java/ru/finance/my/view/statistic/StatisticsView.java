package ru.finance.my.view.statistic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.finance.my.AppConfiguration;
import ru.finance.my.dto.statistics.ExpenseCategoryStatistics;
import ru.finance.my.dto.statistics.IncomeCategoryStatistics;
import ru.finance.my.dto.statistics.StatisticsDTO;
import ru.finance.my.dto.statistics.SumAndPercent;
import ru.finance.my.entity.ExpenseCategory;
import ru.finance.my.entity.IncomeCategory;
import ru.finance.my.utils.CookieUtils;
import ru.finance.my.view.AppLayoutBasic;
import ru.finance.my.view.ErrorView;
import ru.finance.my.view.user.LoginView;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;

@Route("/statistic/:type/:time")
public class StatisticsView extends AppLayoutBasic implements BeforeEnterObserver {

  private static final RestTemplate restTemplate = new RestTemplate();

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
        endDate = beforeEnterEvent.getLocation().getQueryParameters().getParameters().get("end-date").get(0);
      }

      try {
        StatisticsDTO statisticsDTO = getStatisticDto();
        setContent(getContentComponent(statisticsDTO));
      } catch (JsonProcessingException | HttpClientErrorException.Forbidden | NullPointerException exception) {
        beforeEnterEvent.rerouteTo(LoginView.class);
      }

    } else {
      beforeEnterEvent.rerouteTo(ErrorView.class);
    }
  }

  public StatisticsView() {
    super(SelectedTab.STATISTIC);
  }

  private Component getContentComponent(StatisticsDTO statisticsDTO) throws JsonProcessingException {
    Div div = new Div();

    Tabs expenseAndIncomeTabs = getExpenseAndIncomeTabs();
    Tabs dateTabs = getDateTabs();
    Component currentPeriodComponent = getCurrentPeriodComponent(getCurrentPeriod());
    Component statisticComponent = (type.equals(EXPENSES)) ? getExpenseStatisticComponent(statisticsDTO) : getIncomeStatisticComponent(statisticsDTO);

    div.add(expenseAndIncomeTabs, dateTabs, currentPeriodComponent, statisticComponent);
    return div;
  }

  private Component getCurrentPeriodComponent(String currentPeriod) {
    HorizontalLayout horizontalLayout = new HorizontalLayout();

    Div divCurrentPeriod = new Div();

    divCurrentPeriod.setText(currentPeriod);
    divCurrentPeriod.getStyle()
        .set("text-align", "center");

    if (!time.equals(PERIOD)) {
      LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);

      RouterLink previous = new RouterLink(StatisticsView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time)));
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

      RouterLink next = new RouterLink(StatisticsView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time)));
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

  private Component getExpenseStatisticComponent(StatisticsDTO statisticsDTO) throws JsonProcessingException {
    H3 sum = new H3(statisticsDTO.getExpenseSum() + " RUB");
    sum.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    Grid<ExpenseCategoryStatistics> expenseCategoryStatisticsGrid = getExpenseCategoryStatisticsGrid();

    VerticalLayout layout = new VerticalLayout();
    layout.add(sum, expenseCategoryStatisticsGrid);
    layout.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return layout;
  }

  private Component getIncomeStatisticComponent(StatisticsDTO statisticsDTO) throws JsonProcessingException {
    H3 sum = new H3(statisticsDTO.getIncomeSum() + " RUB");
    sum.getStyle()
        .set("margin-left", "auto")
        .set("margin-right", "auto");
    Grid<IncomeCategoryStatistics> incomeCategoryStatisticsGrid = getIncomeCategoryStatistics();

    VerticalLayout layout = new VerticalLayout();
    layout.add(sum, incomeCategoryStatisticsGrid);
    layout.getStyle()
        .set("width", "500px")
        .set("margin-left", "auto")
        .set("margin-right", "auto");

    return layout;
  }

  private Grid<ExpenseCategoryStatistics> getExpenseCategoryStatisticsGrid() throws JsonProcessingException {
    Grid<ExpenseCategoryStatistics> expenseCategoryStatisticsGrid = new Grid<>(ExpenseCategoryStatistics.class, false);
    expenseCategoryStatisticsGrid.addColumn(ExpenseCategoryStatistics::getExpenseCategoryName).setHeader("Name");
    expenseCategoryStatisticsGrid.addColumn(ExpenseCategoryStatistics::getSum).setHeader("Sum, RUB").setSortable(true);
    expenseCategoryStatisticsGrid.addColumn(ExpenseCategoryStatistics::getPercent).setHeader("Percent, %").setSortable(true);

    expenseCategoryStatisticsGrid.setAllRowsVisible(true);
    expenseCategoryStatisticsGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    expenseCategoryStatisticsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    expenseCategoryStatisticsGrid.setItems(getStatisticDto().getPercentOfSumOfExpenseCategory().values());

    return expenseCategoryStatisticsGrid;
  }

  private Grid<IncomeCategoryStatistics> getIncomeCategoryStatistics() throws JsonProcessingException {
    Grid<IncomeCategoryStatistics> incomeCategoryStatisticsGrid = new Grid<>(IncomeCategoryStatistics.class, false);
    incomeCategoryStatisticsGrid.addColumn(IncomeCategoryStatistics::getIncomeCategoryName).setHeader("Name");
    incomeCategoryStatisticsGrid.addColumn(IncomeCategoryStatistics::getSum).setHeader("Sum, RUB").setSortable(true);
    incomeCategoryStatisticsGrid.addColumn(IncomeCategoryStatistics::getPercent).setHeader("Percent, %").setSortable(true);

    incomeCategoryStatisticsGrid.setAllRowsVisible(true);
    incomeCategoryStatisticsGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    incomeCategoryStatisticsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    incomeCategoryStatisticsGrid.setItems(getStatisticDto().getPercentOfSumOfIncomeCategory().values());

    return incomeCategoryStatisticsGrid;
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
    RouterLink statisticExpenses = new RouterLink();
    statisticExpenses.setText("Expenses");
    statisticExpenses.setRoute(StatisticsView.class, new RouteParameters(new RouteParam("type", EXPENSES), new RouteParam("time", time)));
    Map<String, List<String>> parameter = new HashMap<>();
    parameter.put("current-day", List.of(String.valueOf(currentDay)));
    parameter.put("current-month", List.of(String.valueOf(currentMonth)));
    parameter.put("current-year", List.of(String.valueOf(currentYear)));
    if (time.equals(PERIOD)) {
      parameter.put("end-date", List.of(endDate));
    }
    statisticExpenses.setQueryParameters(new QueryParameters(parameter));
    return statisticExpenses;
  }

  private RouterLink getAllCategoriesIncomesRouterLink() {
    RouterLink statisticIncomes = new RouterLink();
    statisticIncomes.setText("Incomes");
    statisticIncomes.setRoute(StatisticsView.class, new RouteParameters(new RouteParam("type", INCOMES), new RouteParam("time", time)));
    Map<String, List<String>> parameter = new HashMap<>();
    parameter.put("current-day", List.of(String.valueOf(currentDay)));
    parameter.put("current-month", List.of(String.valueOf(currentMonth)));
    parameter.put("current-year", List.of(String.valueOf(currentYear)));
    if (time.equals(PERIOD)) {
      parameter.put("end-date", List.of(endDate));
    }
    statisticIncomes.setQueryParameters(new QueryParameters(parameter));
    return statisticIncomes;
  }

  private Tabs getDateTabs() {
    Tabs tabs = new Tabs();
    Tab dayTab = new Tab(getStatisticsTimeRouterLink("Day"));
    Tab weekTab = new Tab(getStatisticsTimeRouterLink("Week"));
    Tab monthTab = new Tab(getStatisticsTimeRouterLink("Month"));
    Tab yearTab = new Tab(getStatisticsTimeRouterLink("Year"));
    Tab periodTab = new Tab(getPeriodButton());

    weekTab.setSelected(true);

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

    RouterLink confirmLink = new RouterLink(StatisticsView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", PERIOD)));
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

  private RouterLink getStatisticsTimeRouterLink(String time) {
    RouterLink statistic = new RouterLink();
    statistic.setText(time);
    statistic.setRoute(StatisticsView.class, new RouteParameters(new RouteParam("type", type), new RouteParam("time", time.toLowerCase())));
    Map<String, List<String>> parameter = new HashMap<>();
    parameter.put("current-day", List.of(String.valueOf(currentDay)));
    parameter.put("current-month", List.of(String.valueOf(currentMonth)));
    parameter.put("current-year", List.of(String.valueOf(currentYear)));
    statistic.setQueryParameters(new QueryParameters(parameter));
    return statistic;
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

  private StatisticsDTO getStatisticDto() throws JsonProcessingException {
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
    String url = AppConfiguration.SERVER_HOST + "/statistics?before-date=" + beforeDate + "&after-date=" + afterDate;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

    return parseStatisticsDTO(response.getBody());
  }

  private StatisticsDTO parseStatisticsDTO(String str) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode root = objectMapper.readTree(str);
    Long expenseSum = root.get("expenseSum").asLong();
    Long incomeSum = root.get("incomeSum").asLong();

    ObjectNode percentOfSumOfExpenseCategoryNode = (ObjectNode)root.get("percentOfSumOfExpenseCategory");
    ObjectNode percentOfSumOfIncomeCategoryNode = (ObjectNode)root.get("percentOfSumOfIncomeCategory");

    Map<Long, ExpenseCategoryStatistics> percentOfSumOfExpenseCategory = new HashMap<>();
    Map<Long, IncomeCategoryStatistics> percentOfSumOfIncomeCategory = new HashMap<>();

    Iterator<String> percentOfSumOfExpenseCategoryNodeFieldNames = percentOfSumOfExpenseCategoryNode.fieldNames();
    while (percentOfSumOfExpenseCategoryNodeFieldNames.hasNext()) {
      String expenseCategoryIdString = percentOfSumOfExpenseCategoryNodeFieldNames.next();
      Long expenseCategoryId = Long.valueOf(expenseCategoryIdString);
      JsonNode expenseCategoryStatisticsNode = percentOfSumOfExpenseCategoryNode.get(expenseCategoryIdString);
      JsonNode sumAndPercentNode = expenseCategoryStatisticsNode.get("sumAndPercent");
      SumAndPercent sumAndPercent = SumAndPercent.builder()
          .percent(sumAndPercentNode.get("percent").asLong())
          .sum(sumAndPercentNode.get("sum").asLong())
          .build();
      JsonNode expenseCategoryNode = expenseCategoryStatisticsNode.get("expenseCategory");
      ExpenseCategory expenseCategory = ExpenseCategory.builder()
          .id(expenseCategoryId)
          .name(expenseCategoryNode.get("name").asText())
          .accessToDelete(expenseCategoryNode.get("accessToDelete").asBoolean())
          .userId(expenseCategoryNode.get("userId").asLong())
          .build();
      ExpenseCategoryStatistics expenseCategoryStatistics = ExpenseCategoryStatistics.builder()
          .sumAndPercent(sumAndPercent)
          .expenseCategory(expenseCategory)
          .build();
      percentOfSumOfExpenseCategory.put(expenseCategoryId, expenseCategoryStatistics);
    }

    Iterator<String> percentOfSumOfIncomeCategoryNodeFieldNames = percentOfSumOfIncomeCategoryNode.fieldNames();
    while (percentOfSumOfIncomeCategoryNodeFieldNames.hasNext()) {
      String incomeCategoryIdString = percentOfSumOfIncomeCategoryNodeFieldNames.next();
      Long incomeCategoryId = Long.valueOf(incomeCategoryIdString);
      JsonNode incomeCategoryStatisticsNode = percentOfSumOfIncomeCategoryNode.get(incomeCategoryIdString);
      JsonNode sumAndPercentNode = incomeCategoryStatisticsNode.get("sumAndPercent");
      SumAndPercent sumAndPercent = SumAndPercent.builder()
          .percent(sumAndPercentNode.get("percent").asLong())
          .sum(sumAndPercentNode.get("sum").asLong())
          .build();
      JsonNode incomeCategoryNode = incomeCategoryStatisticsNode.get("incomeCategory");
      IncomeCategory incomeCategory = IncomeCategory.builder()
          .id(incomeCategoryId)
          .name(incomeCategoryNode.get("name").asText())
          .accessToDelete(incomeCategoryNode.get("accessToDelete").asBoolean())
          .userId(incomeCategoryNode.get("userId").asLong())
          .build();
      IncomeCategoryStatistics incomeCategoryStatistics = IncomeCategoryStatistics.builder()
          .sumAndPercent(sumAndPercent)
          .incomeCategory(incomeCategory)
          .build();
      percentOfSumOfIncomeCategory.put(incomeCategoryId, incomeCategoryStatistics);
    }

    return StatisticsDTO.builder()
        .expenseSum(expenseSum)
        .incomeSum(incomeSum)
        .percentOfSumOfExpenseCategory(percentOfSumOfExpenseCategory)
        .percentOfSumOfIncomeCategory(percentOfSumOfIncomeCategory)
        .build();
  }
}
