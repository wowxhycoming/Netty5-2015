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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.math.BigInteger;
import java.util.List;

/**
 * @author doctor
 *
 * @time 2015年7月7日
 * 
 */
public final class BigIntegerDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 按照编码 传输块：F（标识—） + 内容长度 + 内容 -> 1个字节 + 4个字节 + 内容所占字节数。解析
		// 需要处理粘包/拆包问题。
		if (in.readableBytes() < 5) {
			return; // F（标识—） + 内容长度还没到达。
		}

		in.markReaderIndex();
		short magicNumber = in.readUnsignedByte();// 验证F标识
		if (magicNumber != 'F') {
			in.resetReaderIndex();
			throw new RuntimeException("magicNumber must be 'F' ");
		}

		// 然后读取内容长度，开始读取内容体。
		int dataLength = in.readInt();

		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}

		byte[] data = new byte[dataLength];
		in.readBytes(data);
		out.add(new BigInteger(data));
	}

}
