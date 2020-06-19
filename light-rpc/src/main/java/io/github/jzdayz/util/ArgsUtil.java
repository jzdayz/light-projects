package io.github.jzdayz.util;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArgsUtil {

    public static final Object[] NULL_OBJ_ARRAY = new Object[0];
    private static final char TYPE_DELIMITER = 1;
    private static final char ARG_DELIMITER = 2;

    public static Object[] decode(String args) {
        if (args == null || args.length() == 0) {
            return NULL_OBJ_ARRAY;
        }
        StringBuilder sb = new StringBuilder(100);
        List<Object> res = new ArrayList<>(10);
        Class<?> cla = null;
        for (char c : args.toCharArray()) {
            if (c == TYPE_DELIMITER) {
                cla = Objects.requireNonNull(ReflectionUtil.forName(sb.toString()), "parse error");
                sb.delete(0, Integer.MAX_VALUE);
            }
            if (c == ARG_DELIMITER) {
                res.add(
                        JSON.parseObject(sb.toString(), cla)
                );
                sb.delete(0, Integer.MAX_VALUE);
            }
            sb.append(c);
        }
        return res.toArray();
    }

    public static String encode(Object[] args) {
        StringBuilder sb = new StringBuilder(args.length * 100);
        for (Object arg : args) {
            sb.append(arg.getClass().getName());
            sb.append(TYPE_DELIMITER);
            sb.append(JSON.toJSONString(arg));
            sb.append(ARG_DELIMITER);
        }
        return sb.toString();
    }

}
