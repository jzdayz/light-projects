package io.github.jzdayz.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RpcRegister {

  public static final RpcRegister INSTANCE = new RpcRegister();
  private Map<String, RBean> providerContainer = new ConcurrentHashMap<>(256);
  private Map<String, Object> consumeContainer = new ConcurrentHashMap<>(256);

  public void registerProvider(Object object) {
    registerProvider(object, object.getClass().getName());
  }

  public void registerProvider(Object object, String id) {
    if (id == null || id.length() == 0) {
      registerProvider(object);
    }
    Map<String, Method> maps = Arrays
        .stream(object.getClass().getDeclaredMethods())
        .filter(method -> {
          method.setAccessible(true);
          if (Modifier.isPublic(method.getModifiers())
              && !Modifier.isStatic(method.getModifiers())) {
            return true;
          }
          return false;
        })
        .collect(Collectors.toMap(Method::getName, Function.identity()));
    RpcRegister.INSTANCE.providerContainer
        .put(id, RBean.builder().id(id).methods(maps).bean(object).build());
  }

  public Object registerConsume(Object object, String id) {
    if (id == null || id.length() == 0) {
      id = object.getClass().getName();
    }
    consumeContainer.put(id, object);
    return object;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RBean {

    private String id;
    private Object bean;
    private Map<String, Method> methods;
  }

}
