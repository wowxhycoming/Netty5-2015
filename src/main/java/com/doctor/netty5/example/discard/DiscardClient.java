package com.doctor.netty5.example.discard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class DiscardClient {
	private String host;
	private int port;

	public DiscardClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) {
		DiscardClient discardClient = new DiscardClient("localhost", 8009);
		discardClient.run();

	}

	public void run(){
		
	}
	private static class DiscardClientHandler extends ChannelHandlerAdapter {
		private ByteBuf byteBuf;
		private ChannelHandlerContext context;
		private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			this.context = ctx;
			byteBuf = ctx.alloc().directBuffer(256).writeZero(256);
			generateTraffic();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			byteBuf.release();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
			bufferedReader.close();
		}

		private void generateTraffic() {
			byteBuf.clear();
			String content = "";
			try {
				content = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			byteBuf.writeBytes(content.getBytes(StandardCharsets.UTF_8));

			context.writeAndFlush(byteBuf).addListener((ChannelFuture f) -> {
				if (f.isSuccess()) {
					generateTraffic();
				} else {
					f.cause().printStackTrace();
					f.channel().close();
				}
			});
		}
	}
}
