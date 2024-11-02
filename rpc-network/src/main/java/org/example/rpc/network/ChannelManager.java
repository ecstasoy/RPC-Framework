package org.example.rpc.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to manage channels.
 */
@Slf4j
public class ChannelManager {

  public static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
  private static Bootstrap b;

  public static void setBootstrap(Bootstrap bootstrap) {
    b = bootstrap;
  }

  static Channel connect(InetSocketAddress address) throws Exception {
    CompletableFuture<Channel> future = new CompletableFuture<>();
    b.connect(address).addListener((ChannelFutureListener) f -> {
      if (f.isSuccess()) {
        log.info("Successfully connected to: {}.", address.toString());
        Channel channel = f.channel();
        set(address, channel);  // Store the connected channel in the map
        future.complete(channel);
      } else {
        log.error("Failed to connect to {}: {}", address.toString(), f.cause().getMessage());
        future.completeExceptionally(f.cause());  // Propagate the cause of failure
      }
    });

    return future.get();  // Handle exception when future fails
  }

  /**
   * Get a channel for the given address.
   * If no channel exists, create a new connection.
   * If the existing channel is inactive, remove it and create a new connection.
   */
  public static Channel get(InetSocketAddress address) {
    final String s = address.toString();
    Channel channel = CHANNEL_MAP.get(s);

    if (channel == null) {
      log.info("No existing channel found for {}. Initiating new connection.", address.toString());
      try {
        return connect(address);
      } catch (Exception e) {
        log.error("Failed to connect to {}: {}", address.toString(), e.getMessage());
        return null;
      }
    }

    if (channel.isActive()) {
      log.debug("Reusing active channel for {}.", address.toString());
      return channel;
    } else {
      log.info("Channel for {} is inactive. Removing and reconnecting.", address.toString());
      CHANNEL_MAP.remove(s);
      try {
        return connect(address);
      } catch (Exception e) {
        log.error("Failed to reconnect to {}: {}", address.toString(), e.getMessage());
        return null;
      }
    }
  }

  /**
   * Store the channel in the map.
   */
  public static void set(InetSocketAddress address, Channel channel) {
    final String s = address.toString();
    CHANNEL_MAP.put(s, channel);
    log.info("Channel added for {}.", s);
  }

  /**
   * Remove the channel from the map.
   */
  public static void remove(InetSocketAddress address) {
    final String s = address.toString();
    CHANNEL_MAP.remove(s);
    log.info("Channel removed for {}.", s);
  }
}