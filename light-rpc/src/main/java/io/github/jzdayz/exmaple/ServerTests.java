package io.github.jzdayz.exmaple;

import io.github.jzdayz.server.Server;

public class ServerTests {

  public static void main(String[] args) {
    Server server = new Server(10999);
    server.start();
//        RpcRegister.INSTANCE.register(new Provider(),"jzdayz");
  }

  public static class Provider {

    public People getSomething(People a) {
      return a;
    }

  }

}
