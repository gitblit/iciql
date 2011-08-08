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
package com.iciql.test.models;

import java.util.Random;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * Primitive types model.
 */
@IQTable(name = "PrimitivesTest")
public class PrimitivesModel {

	@IQColumn(primaryKey = true)
	public long myLong;

	@IQColumn
	public int myInteger;

	@IQColumn
	public short myShort;

	@IQColumn
	public byte myByte;

	@IQColumn
	public boolean myBoolean;

	@IQColumn
	public double myDouble;

	@IQColumn
	public float myFloat;

	public PrimitivesModel() {
		Random rand = new Random();
		myLong = rand.nextLong();
		myInteger = rand.nextInt();
		myShort = (short) rand.nextInt(Short.MAX_VALUE);
		myByte = (byte) rand.nextInt(Byte.MAX_VALUE);
		myBoolean = rand.nextInt(1) == 1;
		myDouble = rand.nextDouble();
		myFloat = rand.nextFloat();
	}

	public boolean equivalentTo(PrimitivesModel p) {
		boolean same = true;
		same &= myLong == p.myLong;
		same &= myInteger == p.myInteger;
		same &= myShort == p.myShort;
		same &= myByte == p.myByte;
		same &= myBoolean == p.myBoolean;
		same &= myDouble == p.myDouble;
		same &= myFloat == p.myFloat;
		return same;
	}
}
