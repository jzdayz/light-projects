package io.github.jzdayz.util;

import java.util.Collection;

public abstract class Assert {

  public static void notEmpty(Collection<?> collection, String message) {
    if (collection == null || collection.size() == 0) {
      throw new IllegalArgumentException(message);
    }
  }
}
