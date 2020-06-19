package io.github.jzdayz.server;

import com.alibaba.fastjson.JSON;
import io.github.jzdayz.protocol.Header;
import io.github.jzdayz.protocol.Request;
import io.github.jzdayz.protocol.Response;
import io.github.jzdayz.util.ArgsUtil;
import io.github.jzdayz.util.Constant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    private final AtomicInteger id = new AtomicInteger();

    private ExecutorService workPool = new ThreadPoolExecutor(
            10, 50, 60_000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000),
            (r) -> {
                Thread thread = new Thread(r);
                thread.setName(String.format("Work-Thread-%s", id.incrementAndGet()));
                return thread;
            }
    );


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request res) throws Exception {
        workPool.submit(() -> {
            String id = res.getHeader().get(Constant.Header.ID);
            String method = res.getHeader().get(Constant.Header.METHOD);
            String uuid = res.getHeader().get(Constant.Header.UUID);
            String args = res.getHeader().get(Constant.Header.ARGS);
            try {
                Object result = RpcManager.invoke(id, method, ArgsUtil.decode(args));
                byte[] bytes = JSON.toJSONBytes(result);
                Map<String, String> head = new HashMap<>();
                head.put(Constant.Header.UUID, uuid);
                Response response = Response.builder().header(Header.builder().map(head).build()).body(bytes).build();
                byte[] resBytes = Response.encode(response);
                ctx.writeAndFlush(ctx.alloc().buffer(resBytes.length).writeBytes(resBytes));
            } catch (Exception e) {
                log.error("error", e);
            }
        });
    }
}
