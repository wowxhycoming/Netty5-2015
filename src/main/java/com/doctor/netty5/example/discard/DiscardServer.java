package com.doctor.netty5.example.discard;

import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * telnet 0.0.0.0 8009 测试
 * 
 * @author doctor
 *
 * @time 2015年4月1日 下午5:28:24
 */
public class DiscardServer {
	private int port = 8009;

	public DiscardServer() {
		this(8009);
	}

	public DiscardServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		DiscardServer discardServer = new DiscardServer();
		discardServer.run();
	}

	public  void run() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new DiscardServerHandler());

						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture channelFuture = serverBootstrap.bind(this.port).sync();

			channelFuture.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}

	private static class DiscardServerHandler extends ChannelHandlerAdapter {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			try {
				ByteBuf buf = (ByteBuf) msg;
				System.out.println(buf.toString(StandardCharsets.UTF_8));
				System.out.flush();

			} finally {
				ReferenceCountUtil.release(msg);
			}
		}

	}
}
