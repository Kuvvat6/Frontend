package ru.finance.my;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server-host")
public class AppConfiguration {

  public static String SERVER_HOST;

  public String getValue() {
    return SERVER_HOST;
  }

  public void setValue(String value) {
    SERVER_HOST = value;
  }
}
