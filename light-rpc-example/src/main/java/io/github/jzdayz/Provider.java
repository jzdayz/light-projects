package io.github.jzdayz;

import io.github.jzdayz.annotation.RpcProvider;

@RpcProvider("a")
public class Provider {
    public String get() {
        return "hello";
    }
}
