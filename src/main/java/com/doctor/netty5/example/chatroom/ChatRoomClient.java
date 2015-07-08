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
package com.doctor.netty5.example.chatroom;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * console chatroom client
 * 
 * @author doctor
 *
 * @time 2015年7月8日 下午2:43:16
 */
public class ChatRoomClient {

	private final String host;
	private final int port;

	public ChatRoomClient() {
		this("localhost", 8989);
	}

	public ChatRoomClient(String host) {
		this(host, 8989);
	}

	public ChatRoomClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new ChatRoomClient().start();

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
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8049, true, Delimiters.lineDelimiter()));
							ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new ChatRoomClientHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();

		} finally {
			workersGroup.shutdownGracefully();
		}
	}

	private static class ChatRoomClientHandler extends SimpleChannelInboundHandler<String> {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close().addListener((ChannelFuture f) -> {
				if (f.isSuccess()) {
					System.out.println("网络成功关闭");
				} else {
					System.err.println("网络关闭失败");
				}
			});
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			new Thread(new Hander(ctx)).start();
		}

		@Override
		protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
			System.out.println("服务器返回:" + msg);

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
			boolean writeAble = true;
			ChannelFuture lastChannelFuture = null;
			while (writeAble) {
				String nextLine = scanner.nextLine();
				lastChannelFuture = ctx.writeAndFlush(nextLine + "\n");
				if ("bye".equalsIgnoreCase(nextLine)) {
					try {
						ctx.close().sync();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						writeAble = false;
					}
				}
			}

			if (lastChannelFuture != null) {
				try {
					lastChannelFuture.sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
