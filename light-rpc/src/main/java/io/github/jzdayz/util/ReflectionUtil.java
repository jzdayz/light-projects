package io.github.jzdayz.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionUtil {

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.warn("Can not forName : " + className);
            return null;
        }
    }

}
