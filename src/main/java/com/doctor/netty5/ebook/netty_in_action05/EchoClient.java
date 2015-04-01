package com.doctor.netty5.ebook.netty_in_action05;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.RandomStringUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @author doctor
 *
 * @time 2015年4月1日 下午9:30:01
 */
public class EchoClient {

	private final String host;
	private final int port;

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws InterruptedException {
		NioEventLoopGroup workersGroup = new NioEventLoopGroup(1);

		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workersGroup)
					.channel(NioSocketChannel.class)
					.remoteAddress(host, port)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new EchoClientHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
			workersGroup.shutdownGracefully();
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		new EchoClient("localhost", 8009).start();

	}

	private static class EchoClientHandler extends ChannelHandlerAdapter {
		private final String content = "netty,hello,doctor ,who";

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(Unpooled.copiedBuffer("hello doctor", StandardCharsets.UTF_8));
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ByteBuf byteBuf = (ByteBuf) msg;
			System.out.println("echo:" + byteBuf.toString(StandardCharsets.UTF_8));
			ReferenceCountUtil.release(msg);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(Unpooled.copiedBuffer(RandomStringUtils.random(6, content), StandardCharsets.UTF_8));
		}

	}

}
