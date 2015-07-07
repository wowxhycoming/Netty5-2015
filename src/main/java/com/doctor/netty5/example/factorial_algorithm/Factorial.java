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

import java.math.BigInteger;

/**
 * 阶乘计算Factorial
 * 
 * @author doctor
 *
 * @time 2015年7月7日 下午4:28:33
 */
public final class Factorial {

	public static BigInteger compute(BigInteger bigInteger) {

		if (bigInteger.equals(BigInteger.ONE) || bigInteger.equals(BigInteger.ZERO)) {
			return BigInteger.ONE;
		}

		return bigInteger.multiply(compute(bigInteger.subtract(BigInteger.ONE)));

	}

	public static void main(String[] args) {
		System.out.println(Factorial.compute(BigInteger.valueOf(5)));
	}
}
