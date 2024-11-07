package org.example.rpc.network;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Channel pool.
 *
 * <p>Stores channels for reuse.
 * When a channel is no longer needed, it is returned to the pool.
 *
 * @author Kunhua Huang
 */
@Slf4j
public class ChannelPool {

  private final Map<InetSocketAddress, Queue<Channel>> channelPool = new ConcurrentHashMap<>();
  private final int poolSize;

  /**
   * Constructor.
   *
   * @param poolSize size of the channel pool
   */
  public ChannelPool(int poolSize) {
    this.poolSize = poolSize;
  }

  public Channel getChannel(InetSocketAddress address) {
    Queue<Channel> pool = channelPool.computeIfAbsent(address, adr -> new ConcurrentLinkedQueue<>());

    Channel channel;
    while ((channel = pool.poll()) != null) {
      if (channel.isActive()) {
        return channel;
      } else {
        channel.close();
      }
    }

    return createChannel(address);
  }

  public void returnChannel(InetSocketAddress address, Channel channel) {
    if (channel != null && channel.isActive()) {
      Queue<Channel> pool = channelPool.computeIfAbsent(address, adr -> new ConcurrentLinkedQueue<>());
      if (channelPool.get(address).size() > poolSize) {
        channelPool.get(address).poll().close(); // Close excess channels
      }
    }
  }

  private Channel createChannel(InetSocketAddress address) {
    try {
      Channel channel = ChannelManager.connect(address); // Implement the connect method in ChannelManager
      if (channel != null && channel.isActive()) {
        return channel;
      } else {
        throw new IllegalStateException("Failed to create a new channel to " + address);
      }
    } catch (Exception e) {
      log.error("Error creating channel to {}: {}", address, e.getMessage(), e);
      return null;
    }
  }

}
