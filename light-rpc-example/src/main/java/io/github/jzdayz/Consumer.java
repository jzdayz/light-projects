package io.github.jzdayz;

import io.github.jzdayz.annotation.RpcClient;

@RpcClient("a")
public interface Consumer {

  String get();
}
