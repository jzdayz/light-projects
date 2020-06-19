package io.github.jzdayz;

import io.github.jzdayz.annotation.RpcClient;
import io.github.jzdayz.client.Client;
import io.github.jzdayz.server.RpcRegister;
import java.lang.reflect.Proxy;
import java.util.Objects;
import org.springframework.beans.factory.FactoryBean;

public class LightRpcBeanFactory<T> implements FactoryBean<T> {

  private Class<T> rpcInterface;

  private Client client;

  public LightRpcBeanFactory(Class<T> rpcInterface, Client client) {
    this.rpcInterface = rpcInterface;
    this.client = client;
  }

  @Override
  public T getObject() throws Exception {
    return (T) RpcRegister.INSTANCE.getConsumeContainer()
        .compute(rpcInterface.getName(), (key, oldVal) -> {
          if (oldVal == null) {
            RpcClient annotation = rpcInterface.getAnnotation(RpcClient.class);
            Objects.requireNonNull(annotation);
            Object proxyInstance = Proxy
                .newProxyInstance(this.getClass().getClassLoader(), new Class[]{rpcInterface},
                    (proxy, method, args) -> {
                      String name = method.getName();
                      Class<?> returnType = method.getReturnType();
                      return client.rpc(annotation.value(), name, returnType, args);
                    });
            oldVal = RpcRegister.INSTANCE.registerConsume(proxyInstance, annotation.value());
          }
          return oldVal;
        });
  }

  @Override
  public Class<?> getObjectType() {
    return rpcInterface;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
