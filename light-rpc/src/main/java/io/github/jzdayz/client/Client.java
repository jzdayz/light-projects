package io.github.jzdayz.client;

import com.alibaba.fastjson.JSON;
import io.github.jzdayz.config.Configuration;
import io.github.jzdayz.protocol.Header;
import io.github.jzdayz.protocol.Request;
import io.github.jzdayz.protocol.Response;
import io.github.jzdayz.util.ArgsUtil;
import io.github.jzdayz.util.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

public class Client {

    private Bootstrap bootstrap = null;

    private String address;

    private int port;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WaitResponseFuture{

        private Response response;

        private Thread waiter;

    }

    public static final ConcurrentHashMap<String,WaitResponseFuture> CMD = new ConcurrentHashMap<>(256);

    private  EventLoopGroup work;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public <T> T rpc(String id, String method,Class<T> resType, Object... args) {
        ChannelFuture chanel = getChanel();
        Map<String,String> header = new HashMap<>();
        header.put(Constant.Header.ID,id);
        header.put(Constant.Header.METHOD,method);
        String requestID = UUID.randomUUID().toString();
        header.put(Constant.Header.UUID, requestID);
        Request request = Request.builder().body("Hello!".getBytes(StandardCharsets.UTF_8)).header(Header.builder().map(header).build()).build();
        if (args!=null && args.length>0) {
            header.put(Constant.Header.ARGS, ArgsUtil.encode(args));
        }
        byte[] req = Request.encode(request);

        ByteBufAllocator alloc = chanel.channel().alloc();
        ByteBuf buffer = alloc.buffer(req.length);
        buffer.writeBytes(req);
        CMD.put(requestID, WaitResponseFuture.builder().waiter(Thread.currentThread()).build());
        // 发送请求
        chanel.channel().writeAndFlush(buffer);
        // wait
        LockSupport.parkNanos(Thread.currentThread(),TimeUnit.MILLISECONDS.toNanos(Configuration.getInstance().getClientHandlerTimeout()));
        chanel.channel().close();
        return JSON.parseObject(new String(Objects.requireNonNull(CMD.get(requestID),"no response").getResponse().getBody(),StandardCharsets.UTF_8),resType);
    }

    private ChannelFuture getChanel() {
        if (bootstrap == null) {
            // Configure the client.
            if (work == null) {
                work = new NioEventLoopGroup(1);
            }
            bootstrap = new Bootstrap();
            bootstrap.group(work)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new ClientDecoder());
                        }
                    });
        }
        ChannelFuture connect = bootstrap.connect(address, port);
        // 3s 链接超时
        connect.awaitUninterruptibly(Configuration.getInstance().getClientConnectionTimeout(),TimeUnit.MILLISECONDS);
        if (connect.channel().isActive()) {
            return connect;
        }
        throw new RuntimeException(new TimeoutException("timeout!!"));
    }

    public void close(){
        try {
            work.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
