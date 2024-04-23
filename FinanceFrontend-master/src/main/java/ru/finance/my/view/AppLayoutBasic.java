package ru.finance.my.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import ru.finance.my.view.account.AllAccountsView;
import ru.finance.my.view.category.AllCategoriesView;
import ru.finance.my.view.statistic.StatisticsView;
import ru.finance.my.view.transfer.AllTransfersView;
import ru.finance.my.view.user.UserView;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppLayoutBasic extends AppLayout {

  public enum SelectedTab {
    MAIN,
    ACCOUNTS,
    TRANSFERS,
    STATISTIC,
    CATEGORIES,
    PROFILE,
  }

  public AppLayoutBasic(SelectedTab selectedTab) {
    createNavBar();
    createMenu(selectedTab);
    setPrimarySection(Section.DRAWER);
  }

  private void createNavBar() {
    DrawerToggle toggle = new DrawerToggle();

    HorizontalLayout layoutTitle = new HorizontalLayout();
    Span title = new Span("Finance");
    title.getStyle()
        .set("font-size", "18pt");
    layoutTitle.add(VaadinIcon.MONEY.create(), title);

    addToNavbar(toggle, layoutTitle);
  }

  private void createMenu(SelectedTab selectedTab) {

    RouterLink main = createLink(VaadinIcon.EURO, "Main");
    main.setRoute(MainView.class, new RouteParameters(new RouteParam("type", MainView.EXPENSES), new RouteParam("time", MainView.WEEK)));
    Map<String, List<String>> mainParameters = new HashMap<>();
    mainParameters.put("current-day", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.DAY_OF_MONTH))));
    mainParameters.put("current-month", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.MONTH_OF_YEAR))));
    mainParameters.put("current-year", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.YEAR))));
    main.setQueryParameters(new QueryParameters(mainParameters));

    RouterLink accounts = createLink(VaadinIcon.PIGGY_BANK_COIN, "Accounts");
    accounts.setRoute(AllAccountsView.class);

    RouterLink transfers = createLink(VaadinIcon.MONEY_EXCHANGE, "Transfers");
    transfers.setRoute(AllTransfersView.class, new RouteParameters(new RouteParam("time", AllTransfersView.WEEK)));
    Map<String, List<String>> transfersParameters = new HashMap<>();
    transfersParameters.put("current-day", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.DAY_OF_MONTH))));
    transfersParameters.put("current-month", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.MONTH_OF_YEAR))));
    transfersParameters.put("current-year", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.YEAR))));
    transfers.setQueryParameters(new QueryParameters(transfersParameters));

    RouterLink statistic = createLink(VaadinIcon.BAR_CHART, "Statistic");
    statistic.setRoute(StatisticsView.class, new RouteParameters(new RouteParam("type", StatisticsView.EXPENSES), new RouteParam("time", StatisticsView.WEEK)));
    Map<String, List<String>> statisticParameters = new HashMap<>();
    statisticParameters.put("current-day", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.DAY_OF_MONTH))));
    statisticParameters.put("current-month", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.MONTH_OF_YEAR))));
    statisticParameters.put("current-year", Collections.singletonList(String.valueOf(LocalDate.now().get(ChronoField.YEAR))));
    statistic.setQueryParameters(new QueryParameters(statisticParameters));

    RouterLink categories = createLink(VaadinIcon.ALIGN_JUSTIFY, "Categories");
    categories.setRoute(AllCategoriesView.class, new RouteParameters(new RouteParam("type", AllCategoriesView.EXPENSES)));

    RouterLink profile = createLink(VaadinIcon.USER, "Profile");
    profile.setRoute(UserView.class);

    Tab mainTab = new Tab(main);
    Tab accountsTab = new Tab(accounts);
    Tab transfersTab = new Tab(transfers);
    Tab statisticTab = new Tab(statistic);
    Tab categoriesTab = new Tab(categories);
    Tab profileTab = new Tab(profile);

    Tabs tabs = new Tabs(
        mainTab,
        accountsTab,
        transfersTab,
        statisticTab,
        categoriesTab,
        profileTab
    );

    switch (selectedTab) {
      case MAIN -> tabs.setSelectedTab(mainTab);
      case ACCOUNTS -> tabs.setSelectedTab(accountsTab);
      case TRANSFERS -> tabs.setSelectedTab(transfersTab);
      case STATISTIC -> tabs.setSelectedTab(statisticTab);
      case CATEGORIES -> tabs.setSelectedTab(categoriesTab);
      case PROFILE -> tabs.setSelectedTab(profileTab);
    }

    tabs.setOrientation(Tabs.Orientation.VERTICAL);
    addToDrawer(tabs);
  }

  private RouterLink createLink(VaadinIcon viewIcon, String viewName) {
    Icon icon = viewIcon.create();
    icon.getStyle().set("box-sizing", "border-box")
        .set("margin-inline-end", "var(--lumo-space-m)")
        .set("margin-inline-start", "var(--lumo-space-xs)")
        .set("padding", "var(--lumo-space-xs)");

    RouterLink link = new RouterLink();
    link.add(icon, new Span(viewName));

    return link;
  }
}
