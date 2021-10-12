package com.github.benslabbert.mylinks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.benslabbert.mylinks.exception.ConfigException;
import com.github.benslabbert.mylinks.factory.StorageServiceFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record Config(Redis redis) {

  private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

  public static final Config INSTANCE;

  static {
    LOGGER.info("loading config");
    var mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();

    var optionalConfig = Optional.ofNullable(System.getenv("API_SERVER_CONFIG"));

    try {
      InputStream inputStream;
      if (optionalConfig.isPresent()) {
        inputStream = Files.newInputStream(Paths.get(optionalConfig.get()));
      } else {
        inputStream =
            StorageServiceFactory.class.getClassLoader().getResourceAsStream("config.yaml");
      }

      INSTANCE = mapper.readValue(inputStream, Config.class);
    } catch (Exception e) {
      throw new ConfigException("unable to read config", e);
    }
  }

  // needed for jackson
  public Config() {
    this(null);
  }

  public record Redis(String host, int port) {

    // needed for jackson
    public Redis() {
      this("", 0);
    }
  }
}
