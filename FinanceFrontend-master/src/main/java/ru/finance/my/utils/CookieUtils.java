package ru.finance.my.utils;

import com.vaadin.flow.server.VaadinRequest;

import javax.servlet.http.Cookie;

public class CookieUtils {

  public static Cookie getCookieByNameFromRequest(String name, VaadinRequest request) {
    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(name)) {
        return cookie;
      }
    }
    return null;
  }
}
