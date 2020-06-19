package io.github.jzdayz.server;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcManager {

  public static Object invoke(String id, String methodName, Object... args) throws Exception {
    RpcRegister.RBean rBean = RpcRegister.INSTANCE.getProviderContainer().get(id);
    Method method = rBean.getMethods().get(methodName);
    try {
      return method.invoke(rBean.getBean(), args);
    } catch (Exception e) {
      log.error("method invoke error", e);
      throw new RuntimeException(e);
    }
  }
}
