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
package com.xxx.netty.run;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.xxx.netty.handle.SecureChatServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.example.telnet.TelnetServer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Simple SSL chat server modified from {@link TelnetServer}.
 */
@Component
public final class SecureChatServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureChatServer.class);
    

	public static Map<String, Channel> channelMap = new HashMap<String, Channel>();
    
	@Value("${server.port}")
	int PORT ;

	//public static ShardedJedis jedis;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:root-context.xml");// loading
		//jedis = context.getBean(RedisInitBean.class).getSingletonInstance();
		SecureChatServer chatServer=context.getBean(SecureChatServer.class); 
		// SelfSignedCertificate是一个用于管理可信消息的工厂管理者
		SelfSignedCertificate ssc = new SelfSignedCertificate();
		// 工厂；授权和发给私钥
		SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();// 服务引导程序，服务器端快速启动程序
			serverBootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new SecureChatServerInitializer(sslCtx));
			if(null!=args && args.length>1 && args[0].matches("\\d")){
				chatServer.PORT=Integer.parseInt(args[0]);
			}
			LOGGER.debug("SSL TCP server started on port:{}",chatServer.PORT);
			serverBootstrap.bind(chatServer.PORT).sync().channel().closeFuture().sync();

		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			context = null;
		}
	}
	
}
