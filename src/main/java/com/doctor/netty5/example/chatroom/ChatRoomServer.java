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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;

import java.nio.charset.StandardCharsets;

/**
 * console chatroom server
 * 
 * @author doctor
 *
 * @time 2015年7月8日 下午2:42:58
 */
public class ChatRoomServer {
	private final int port;

	public ChatRoomServer() {
		this(8989);
	}

	public ChatRoomServer(int port) {
		this.port = port;
	}

	/**
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new ChatRoomServer().start();

	}

	public void start() throws InterruptedException {
		ServerBootstrap bootstrap = new ServerBootstrap();
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(port)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8049, true, Delimiters.lineDelimiter()));
							ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
							ch.pipeline().addLast(new ChatRoomServerHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.bind().sync();
			System.out.println(ChatRoomServer.class.getName() + " started and listen on port:" + channelFuture.channel().localAddress());

			channelFuture.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	private static class ChatRoomServerHandler extends SimpleChannelInboundHandler<String> {
		// 保存所有客户端连接/静态变量
		private static final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventLoop());

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			channels.add(ctx.channel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close().addListener(ChatRoomServerHandler::closeOperationComplete);
		}

		@Override
		protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
			channels.forEach(channel -> {
				if (channel.id().equals(ctx.channel().id())) {
					channel.writeAndFlush("[ you ] " + msg + "\n");
				} else {
					channel.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " + msg + "\n");
				}
			});

			if ("bye".equalsIgnoreCase(msg)) {
				ctx.close().addListener(ChatRoomServerHandler::closeOperationComplete);
			}

		}

		public static void closeOperationComplete(Future<? super Void> f) throws Exception {
			ChannelFuture channelFuture = (ChannelFuture) f;
			if (channelFuture.isSuccess()) {
				channels.remove(channelFuture.channel());
				System.out.println(channelFuture.channel().remoteAddress() + "网络成功关闭");

			} else {
				System.err.println(channelFuture.channel().remoteAddress() + "网络关闭失败");
			}
			channels.remove(channelFuture.cause());
		}
	}

}
