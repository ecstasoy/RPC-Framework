package org.example.rpc.core.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel manager
 */
@Slf4j
public class ChannelManager {

  public static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

  private static Bootstrap b;

  public static void setBootstrap(Bootstrap bootstrap) {
    b = bootstrap;
  }


  @SneakyThrows
  private static Channel connect(InetSocketAddress address) {
    CompletableFuture<Channel> future = new CompletableFuture<>();
    b.connect(address).addListener((ChannelFutureListener) f -> {
      if (f.isSuccess()) {
        log.info("Successfully connect to: {}.", address.toString());
        future.complete(f.channel());
      } else {
        throw new IllegalStateException();
      }
    });

    return future.get();
  }

  /**
   * Get channel
   *
   * @param address address
   * @return channel
   */
  public static Channel get(InetSocketAddress address) {
    final String s = address.toString();
//        if (!CHANNEL_MAP.containsKey(s)) {
//            return null;
//        }

    final Channel channel = CHANNEL_MAP.get(s);
    if (channel == null) {
      return connect(address);
    }

    if (channel.isActive()) {
      return channel;
    } else {
      CHANNEL_MAP.remove(s);
      return null;
    }
  }

  /**
   * Set channel
   *
   * @param address address
   * @param channel channel
   */
  public static void set(InetSocketAddress address, Channel channel) {
    final String s = address.toString();
    CHANNEL_MAP.put(s, channel);
  }

  /**
   * Remove channel
   *
   * @param address address
   */
  public static void remove(InetSocketAddress address) {
    final String s = address.toString();
    CHANNEL_MAP.remove(s);
  }

}