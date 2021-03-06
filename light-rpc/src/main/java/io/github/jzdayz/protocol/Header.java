package io.github.jzdayz.protocol;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Header {

  private Map<String, String> map = new HashMap<>();

  public String get(String key) {
    return map.get(key);
  }

  public String set(String key, String val) {
    return map.put(key, val);
  }
}
