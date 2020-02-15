package io.github.jzdayz;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class App {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
        Consumer bean = run.getBean(Consumer.class);
        TimeUnit.MILLISECONDS.sleep(200L);
        System.out.println(bean.get());
        System.exit(1);
    }
}
