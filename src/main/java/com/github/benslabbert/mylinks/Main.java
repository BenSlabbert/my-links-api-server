package com.github.benslabbert.mylinks;

import com.github.benslabbert.mylinks.handler.HttpBusinessHandler;
import com.github.benslabbert.mylinks.thread.MyThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws InterruptedException {
    var poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(4);
    poolConfig.setMaxIdle(4);

    var bossGroup = new EpollEventLoopGroup(1, new MyThreadFactory("boss"));
    var childGroup = new EpollEventLoopGroup(2, new MyThreadFactory("child"));

    try (var jedisPool = new JedisPool(poolConfig, "localhost")) {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, childGroup)
          .channel(EpollServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                  var p = ch.pipeline();
                  p.addLast("ideStateHandler", new IdleStateHandler(10, 10, 10));
                  p.addLast("codec", new HttpServerCodec());
                  p.addLast("aggregator", new HttpObjectAggregator(Short.MAX_VALUE));
                  p.addLast("compressor", new HttpContentCompressor());
                  p.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
                  p.addLast("businessLogicHandler", new HttpBusinessHandler(jedisPool));
                }
              });

      // Bind and start to accept incoming connections.
      ChannelFuture f = b.bind(8080).sync();

      // Wait until the server socket is closed.
      // In this example, this does not happen, but you can do that to gracefully
      // shut down your server.
      f.channel().closeFuture().sync();
    } finally {
      log.info("stopping thread pools");

      if (ForkJoinPool.commonPool().awaitQuiescence(10L, TimeUnit.SECONDS)) {
        log.info("ForkJoinPool.commonPool stopped all tasks");
      } else {
        log.warn("not able to complete all tasks");
      }

      childGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }

    log.info("exit");
  }
}
