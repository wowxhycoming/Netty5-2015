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
package com.doctor.netty5.example.echo_object;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Scanner;

/**
 * ObjectEncoder、ObjectDecoder对象解码器
 * 
 * @author doctor
 *
 * @time 2015年7月7日 下午1:41:42
 * 
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
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.softCachingResolver(this.getClass().getClassLoader())));
							ch.pipeline().addLast(new EchoClientHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
			workersGroup.shutdownGracefully();
		}
	}

	private static class EchoClientHandler extends SimpleChannelInboundHandler<Request> {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(new Request("请求连接:", "我要连接...."));
			new Thread(new Hander(ctx)).start();
		}

		@Override
		protected void messageReceived(ChannelHandlerContext ctx, Request msg) throws Exception {
			System.out.println("服务器返回：" + msg);

		}
	}

	private static class Hander implements Runnable {
		private ChannelHandlerContext ctx = null;
		private Scanner scanner = new Scanner(System.in);
		private int count = 0;

		public Hander(ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			while (true) {
				String nextLine = scanner.nextLine();
				ctx.writeAndFlush(new Request("第" + count + "请求：", nextLine));
				count++;
			}

		}
	}

}
