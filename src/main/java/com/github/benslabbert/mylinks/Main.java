package com.github.benslabbert.mylinks;

import com.github.benslabbert.mylinks.factory.StorageServiceFactory;
import com.github.benslabbert.mylinks.handler.HttpBusinessHandler;
import com.github.benslabbert.mylinks.thread.MyThreadFactory;
import com.google.gson.Gson;
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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws InterruptedException {
    var workerGroup = new DefaultEventExecutorGroup(4, new MyThreadFactory("worker"));
    var bossGroup = new EpollEventLoopGroup(1, new MyThreadFactory("boss"));
    var childGroup = new EpollEventLoopGroup(2, new MyThreadFactory("child"));

    try (var jedis = StorageServiceFactory.POOL.getResource()) {
      String pong = jedis.ping();
      LOGGER.info("ping: {}", pong);
    } catch (JedisConnectionException e) {
      LOGGER.error("unable to connect to redis", e);
      return;
    }

    try {
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
                  p.addLast("chunkedWrite", new ChunkedWriteHandler());
                  p.addLast(
                      workerGroup,
                      "businessLogic",
                      new HttpBusinessHandler(StorageServiceFactory.INSTANCE, new Gson()));
                }
              });

      // Bind and start to accept incoming connections.
      ChannelFuture f = b.bind(8080).sync();

      // Wait until the server socket is closed.
      // In this example, this does not happen, but you can do that to gracefully
      // shut down your server.
      f.channel().closeFuture().sync();
    } finally {
      LOGGER.info("stopping thread pools");

      workerGroup.shutdownGracefully();
      childGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }

    LOGGER.info("exit");
  }
}
