/*
 * Copyright (C) 2014-present  The  Netty5-2015  Authors
 *
 * https://github.com/sdcuike
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctor.netty5.example.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.doctor.netty5.ebook.netty_in_action05.common.NettyUtil;

/**
 * @author doctor
 *
 * @time 2015年7月6日 下午5:25:28
 */
public class EchoClient {

	private final String host;
	private final int port;

	public EchoClient() {
		this("localhost", 8989);
	}

	public EchoClient(String host) {
		this(host, 8989);
	}

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new EchoClient().start();

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
							ch.pipeline().addLast(new LineBasedFrameDecoder(2048));
							ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new EchoClientHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
			workersGroup.shutdownGracefully();
		}
	}

	private static class EchoClientHandler extends ChannelHandlerAdapter {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(NettyUtil.appenEndOfLine("我要连接...."));
			new Thread(new Hander(ctx)).start();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			try {
				System.out.println(msg);
			} finally {
				// 读完消息记得释放，那写消息为什么不这样操作呢，因为写完消息netty自动释放。
				// 其操作见：DefaultChannelHandlerInvoker L331-332,不过有这个注释-> promise cancelled
				// 是不少netty5正式发布的时候会取消呢。
				// 我们可以使用SimpleChannelInboundHandler作为父类，因为释放操作已实现。
				ReferenceCountUtil.release(msg);
			}

		}
	}

	private static class Hander implements Runnable {
		private ChannelHandlerContext ctx = null;
		private Scanner scanner = new Scanner(System.in);

		public Hander(ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			while (true) {
				String nextLine = scanner.nextLine();
				ctx.writeAndFlush(NettyUtil.appenEndOfLine(nextLine));
			}

		}
	}

}
