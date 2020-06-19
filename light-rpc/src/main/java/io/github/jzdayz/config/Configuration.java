package io.github.jzdayz.config;

import lombok.Data;

@Data
public class Configuration {

  private final static Configuration CONFIGURATION = new Configuration();
  private static final String PREFIX = "light.rpc.";
  private static final String CLIENT_CONNECTION_TIMEOUT = PREFIX + "connectionTimeout";
  private static final String CLIENT_HANDLER_TIMEOUT = PREFIX + "handlerTimeout";
  // ------------------------ client ------------------------
  private int clientConnectionTimeout = getPropertyInt(CLIENT_CONNECTION_TIMEOUT, 3000);
  private int clientHandlerTimeout = getPropertyInt(CLIENT_CONNECTION_TIMEOUT, 3000);

  public static Configuration getInstance() {
    return CONFIGURATION;
  }

  private int getPropertyInt(String key, int defaultVal) {
    return Integer.parseInt(getPropertyString(key, String.valueOf(defaultVal)));
  }

  private String getPropertyString(String key, String defaultVal) {
    String envVal = System.getenv(transEnv(key));
    return System.getProperty(key, envVal == null ?
        defaultVal : envVal);
  }

  private long getPropertyLong(String key, long defaultVal) {
    return Long.parseLong(getPropertyString(key, String.valueOf(defaultVal)));
  }

  private String transEnv(String property) {
    return property.toUpperCase().replace('.', '_');
  }


}
