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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.math.BigInteger;

/**
 * 数字到字节的编码
 * 
 * @author doctor
 *
 * @time 2015年7月7日
 */
@Sharable
public final class NumberEncoder extends MessageToByteEncoder<Number> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Number msg, ByteBuf out) throws Exception {

		if (msg == null) {
			throw new RuntimeException("not null");
		}
		BigInteger integer = null;
		if (msg instanceof BigInteger) {
			integer = (BigInteger) msg;
		} else {
			integer = new BigInteger(String.valueOf(msg));
		}

		byte[] byteArray = integer.toByteArray();
		// 传输块：F（标识—） + 内容长度 + 内容 -> 1个字节 + 4个字节 + 内容所占字节数。
		out.writeByte((byte) 'F');
		out.writeInt(byteArray.length);
		out.writeBytes(byteArray);
	}
}
