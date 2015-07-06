/*
 * Copyright (C) 2014- now() The  Netty5-2015  Authors
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
package com.doctor.netty5.ebook.netty_in_action05.common;

import java.util.stream.Stream;

/**
 * @author doctor
 *
 * @time 2015年7月6日 下午8:40:36
 */
public final class NettyUtil {
	public static final String END_OF_LINE = "\n";

	public static String appenEndOfLine(String... message) {
		StringBuilder stringBuilder = new StringBuilder(256);
		Stream.of(message).forEachOrdered(stringBuilder::append);
		stringBuilder.append(END_OF_LINE);
		return stringBuilder.toString();
	}
}
