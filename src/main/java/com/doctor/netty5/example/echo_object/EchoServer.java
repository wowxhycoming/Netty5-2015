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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * ObjectEncoder、ObjectDecoder对象解码器
 * 
 * @author doctor
 *
 * @time 2015年7月7日 下午1:41:26
 * 
 */
public class EchoServer {
	private final int port;

	public EchoServer() {
		this(8989);
	}

	public EchoServer(int port) {
		this.port = port;
	}

	/**
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new EchoServer().start();

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
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.softCachingResolver(this.getClass().getClassLoader())));
							ch.pipeline().addLast(new EchoServerHandler());
						}
					});

			ChannelFuture channelFuture = bootstrap.bind().sync();
			System.out.println(EchoServer.class.getName() + " started and listen on port:" + channelFuture.channel().localAddress());

			channelFuture.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	private static class EchoServerHandler extends SimpleChannelInboundHandler<Request> {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		protected void messageReceived(ChannelHandlerContext ctx, Request msg) throws Exception {
			System.out.println(msg);
			ctx.write(msg);

		}
	}

}
