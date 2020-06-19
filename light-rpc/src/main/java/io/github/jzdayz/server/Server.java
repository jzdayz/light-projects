package io.github.jzdayz.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class Server {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private List<ChannelHandler> channelHandlersSharable;
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        channelHandlersSharable = Collections.singletonList(new ServerHandler());
        new Thread(this::start0).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void start0() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new ServerDecoder());
                            channelHandlersSharable.forEach(p::addLast);
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            countDownLatch.countDown();
            log.info("Rpc Server Up !!!!!!");
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("server start error", e);
            System.exit(1);
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
