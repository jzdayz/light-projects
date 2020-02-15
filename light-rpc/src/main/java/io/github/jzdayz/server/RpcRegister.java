package io.github.jzdayz.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class RpcRegister {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RBean{
        private String id;
        private Object bean;
        private Map<String, Method> methods;
    }

    private Map<String,RBean> providerContainer = new HashMap<>(256);
    private Map<String,Object> consumeContainer = new HashMap<>(256);

    public static final RpcRegister INSTANCE = new RpcRegister();

    public void registerProvider(Object object){
        registerProvider(object,object.getClass().getName());
    }

    public void registerProvider(Object object,String id){
        if(id == null || id.length() == 0){
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
        RpcRegister.INSTANCE.providerContainer.put(id,RBean.builder().id(id).methods(maps).bean(object).build());
    }

    public Object registerConsume(Object object,String id){
        if (id == null || id.length() == 0){
            id = object.getClass().getName();
        }
        consumeContainer.put(id,object);
        return object;
    }

}
