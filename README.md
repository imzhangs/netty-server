#Netty-5.0 Base on SSL  TCP Simple   Server
 
##1.1   添加maven 核心依赖
``` xml
<properties>
	<springframework.version>4.2.5.RELEASE</springframework.version>
	<netty.version>5.0.0.Alpha2</netty.version>
</properties>

<dependencies>
	<dependency>
		<groupId>io.netty</groupId>
		<artifactId>netty-all</artifactId>
		<version>${netty.version}</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
		<version>${springframework.version}</version>
	</dependency>
</dependencies>
```

##1.2  参数配置
通过spring 注解默认端口参数
``` java
	@Value("${server.port}")
	int PORT ;
```
也可通过main 入口参数载入
``` java
	if(null!=args && args.length>1 && args[0].matches("\\d")){
		chatServer.PORT=Integer.parseInt(args[0]);
	}
```
SSL 协议声明
``` java
	//SelfSignedCertificate是一个用于管理可信消息的工厂管理者
	SelfSignedCertificate ssc = new SelfSignedCertificate();
	// 工厂；授权和发给私钥
	SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
```

##1.3 netty 事件驱动服务端结构
``` java
SecureChatServer     // ServerBootstrap服务引导程序，服务器端快速启动程序
SecureChatServerInitializer  //管道容器初始化，数据包编码解码，指定粘包处理方式和心跳保持配置等
SecureChatServerHandler  extends SimpleChannelInboundHandler <EncodeType>  // 消息事件回调处理   

	/** Handler 需要重载以下事件方法**/
	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		...// 有客户端链接上来，通信被激活时触发
	}

	@Override
	protected void messageReceived(ChannelHandlerContext context, String msg) {
		... //收到消息时触发
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		... //当前管道发生异常时触发
	}
``` 



	
