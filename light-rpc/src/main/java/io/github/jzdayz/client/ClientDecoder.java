package io.github.jzdayz.client;

import io.github.jzdayz.protocol.Response;
import io.github.jzdayz.util.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j
public class ClientDecoder extends LengthFieldBasedFrameDecoder {

    private final static int MB_50 = 50 * 1024 * 1024;
    private final static int maxFrameLength = Integer
            .parseInt(System.getProperty("simple.rpc.maxFrameLength", String.valueOf(MB_50)));

    public ClientDecoder() {
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
            Response decode = Response.decode(frame);
            Client.WaitResponseFuture waitResponseFuture = Client.CMD
                    .get(decode.getHeader().get(Constant.Header.UUID));
            waitResponseFuture.setResponse(decode);
            LockSupport.unpark(waitResponseFuture.getWaiter());
            return null;
        } catch (Exception e) {
            log.error("decode");
            ctx.channel().close().addListener((ChannelFutureListener) future -> log.info("closeChannel"));
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
