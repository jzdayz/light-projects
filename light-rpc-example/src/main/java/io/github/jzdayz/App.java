package io.github.jzdayz;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App {

  public static void main(String[] args) {
    ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
    Consumer bean = run.getBean(Consumer.class);
    System.out.println(bean.get());
    System.exit(1);
  }
}
