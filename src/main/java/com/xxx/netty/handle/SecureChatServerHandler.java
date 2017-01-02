/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.xxx.netty.handle;

import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xxx.netty.run.SecureChatServer;
import com.xxx.util.NetAddressUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Handles a server-side channel.
 */
@Component
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecureChatServerHandler.class);

	String serverInternetIp;

	@Value("${server.port}")
	String serverPort;

	public SecureChatServerHandler() {
		try {
			serverInternetIp = NetAddressUtil.getInetnetIp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {// 客户端启动时调用该方法
		// System.out.println("channelactive!");

		// Once session is secured, send a greeting and register the channel to
		// the global channel
		// list so the channel received the messages from others.
		// 生成系统消息
		ctx.pipeline().get(SslHandler.class).handshakeFuture()
				.addListener(new GenericFutureListener<Future<Channel>>() {
					@Override
					public void operationComplete(Future<Channel> future) throws Exception {
						ctx.writeAndFlush(
								"@Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
						LOGGER.info("connected :{}" + ctx.channel().remoteAddress());
					}
				});

	}

	protected void messageReceived(ChannelHandlerContext context, String msg) throws Exception {
		channelRead(context, msg);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void channelRead(ChannelHandlerContext ctx, String msg) throws Exception {// 每次发送消息时
		LOGGER.info("c2s received=>> :{}" + msg);
		if (msg.contains("@login:")) {
			SecureChatServer.channelMap.put(msg.split(":")[1], ctx.channel());
			ctx.channel().writeAndFlush(msg.split(":")[1] + " login is OK ,welcome!" + '\n');
		} else if (msg.contains("@all:")) {
			for (Map.Entry<String, Channel> client : SecureChatServer.channelMap.entrySet()) { // 对所有通道进行遍历发送消息
				client.getValue().writeAndFlush(msg + '\n').sync();
			}
		}
		if (msg.contains("@to:")) {
			String friend = msg.split(":")[1];
			Channel channel = SecureChatServer.channelMap.get(friend);
			if (null != channel) {
				channel.writeAndFlush(msg + '\n').sync();
			} else {
				ctx.channel().writeAndFlush("no online!").sync();
			}

		}
		// Close the connection if the client has sent 'bye'.
		if (msg.toLowerCase().matches("^[bye|exit|quit]{3,4}$")) {
			ctx.close();
		}
	}

}
