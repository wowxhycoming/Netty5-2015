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
package com.doctor.netty5.example.factorial_algorithm;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.math.BigInteger;
import java.util.Scanner;

/**
 * 自定义对象解码器，处理粘包/拆包问题。
 * 
 * @author doctor
 *
 * @time 2015年7月7日 下午1:41:42
 * 
 */
public class FactorialClient {

	private final String host;
	private final int port;

	public FactorialClient() {
		this("localhost", 8989);
	}

	public FactorialClient(String host) {
		this(host, 8989);
	}

	public FactorialClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new FactorialClient().start();

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
							ch.pipeline().addLast(new NumberEncoder());
							ch.pipeline().addLast(new BigIntegerDecoder());
							ch.pipeline().addLast(new FactorialClientHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
			workersGroup.shutdownGracefully();
		}
	}

	private static class FactorialClientHandler extends SimpleChannelInboundHandler<BigInteger> {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			new Thread(new Hander(ctx)).start();
		}

		@Override
		protected void messageReceived(ChannelHandlerContext ctx, BigInteger msg) throws Exception {
			System.out.println("服务器返回：" + msg + "\r\n");

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
			String nextLine = null;
			BigInteger bigInteger = null;
			while (true) {
				System.out.println();
				System.out.print("请输入整数：");

				try {
					nextLine = scanner.nextLine();
					bigInteger = BigInteger.valueOf(Long.parseLong(nextLine));
				} catch (NumberFormatException e) {
					System.out.println("输入格式错误：");
					continue;
				}
				ctx.writeAndFlush(bigInteger);
			}

		}
	}

}
