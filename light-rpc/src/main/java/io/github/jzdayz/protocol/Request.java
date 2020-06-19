package io.github.jzdayz.protocol;

import com.alibaba.fastjson.JSON;
import io.github.jzdayz.util.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request {

  private Header header;

  private byte[] body;

  /**
   * len + headerLen + header + body
   *
   * @param byteBuf 字节
   * @return request
   */
  public static Request decode(ByteBuf byteBuf) {
    int len = byteBuf.readInt();
    int headerLen = byteBuf.readInt();
    ByteBuf headerBytes = byteBuf.readSlice(headerLen);
    Header header = JSON
        .parseObject(new String(ByteBufUtil.getBytes(headerBytes), StandardCharsets.UTF_8),
            Header.class);
    return Request.builder().header(header)
        .body(ByteBufUtil.getBytes(byteBuf.readSlice(len - 4 - headerLen))).build();
  }

  @SuppressWarnings("DuplicatedCode")
  public static byte[] encode(Request request) {
    byte[] body = request.getBody();
    byte[] headerBytes = JSON.toJSONBytes(request.getHeader());
    byte[] res = new byte[4 + 4 + headerBytes.length + body.length];
    System.arraycopy(ByteUtils.intToByteArray(res.length - 4), 0, res, 0, 4);
    System.arraycopy(ByteUtils.intToByteArray(headerBytes.length), 0, res, 4, 4);
    System.arraycopy(headerBytes, 0, res, 8, headerBytes.length);
    System.arraycopy(body, 0, res, res.length - body.length, body.length);
    return res;
  }


}
