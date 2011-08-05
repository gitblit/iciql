/*
 * Copyright 2011 James Moger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iciql.test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit 4 iciql test suite.
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ AliasMapTest.class, AnnotationsTest.class, BooleanModelTest.class, ClobTest.class,
		ConcurrencyTest.class, ModelsTest.class, SamplesTest.class, UpdateTest.class, RuntimeQueryTest.class,
		StatementLoggerTest.class })
public class IciqlSuite {

	public static void assertStartsWith(String a, String b) {
		Assert.assertTrue(a.startsWith(b));
	}
}
