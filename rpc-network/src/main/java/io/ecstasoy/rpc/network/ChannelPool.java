package io.ecstasoy.rpc.network;

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

  /**
   * Get a channel from the pool.
   *
   * <p>If the pool is empty, a new channel is created.
   *
   * @param address address of the channel
   * @return channel
   */
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

  /**
   * Return a channel to the pool.
   *
   * <p>If the pool is full, the excess channels are closed.
   *
   * @param address address of the channel
   * @param channel channel to return
   */
  public void returnChannel(InetSocketAddress address, Channel channel) {
    if (channel != null && channel.isActive()) {
      Queue<Channel> pool = channelPool.computeIfAbsent(address, adr -> new ConcurrentLinkedQueue<>());
      if (channelPool.get(address).size() > poolSize) {
        channelPool.get(address).poll().close(); // Close excess channels
      }
    }
  }

  /**
   * Create a new channel.
   *
   * @param address address of the channel
   * @return channel
   */
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
