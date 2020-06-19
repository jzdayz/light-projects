package io.github.jzdayz.server;

import io.github.jzdayz.protocol.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerDecoder extends LengthFieldBasedFrameDecoder {

    private final static int MB_50 = 50 * 1024 * 1024;
    private final static int maxFrameLength = Integer.parseInt(System.getProperty("simple.rpc.maxFrameLength", String.valueOf(MB_50)));

    public ServerDecoder() {
        super(maxFrameLength, 0, 4, 0, 0/*解出的数据集，包括代表长度的字节*/);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }
            return Request.decode(frame);
        } catch (Exception e) {
            log.error("decode", e);
            ctx.channel().close().addListener((ChannelFutureListener) future -> log.info("closeChannel"));
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
