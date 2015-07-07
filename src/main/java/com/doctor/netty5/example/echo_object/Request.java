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

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 简单请求封装
 * 
 * @author doctor
 *
 * @time 2015年7月7日 下午1:43:29
 */
public final class Request implements Serializable {
	private static final long serialVersionUID = -6583692011726272727L;
	private final String name;
	private final String content;

	public Request(String name, String content) {
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
