/*
 * Copyright 2004-2011 H2 Group.
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import com.iciql.Iciql.EnumType;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQEnum;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQIndexes;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.IQVersion;
import com.iciql.Iciql.IndexType;
import com.iciql.test.models.EnumModels.Tree;
import com.iciql.util.Utils;

/**
 * A data class that contains a column for each data type.
 */
@IQTable(strictTypeMapping = true)
@IQIndexes({ @IQIndex({ "myLong", "myInteger" }), @IQIndex(type = IndexType.HASH, value = "myString") })
@IQVersion(1)
public class SupportedTypes {

	public static final SupportedTypes SAMPLE = new SupportedTypes();

	/**
	 * Test of plain enumeration.
	 * 
	 * Each field declaraton of this enum must specify a mapping strategy.
	 */
	public enum Flower {
		ROSE, TULIP, MUM, PETUNIA, MARIGOLD, DAFFODIL;
	}

	@IQColumn(primaryKey = true, autoIncrement = true)
	public Integer id;

	@IQColumn
	private Boolean myBool;

	@IQColumn
	private Byte myByte;

	@IQColumn
	private Short myShort;

	@IQColumn
	private Integer myInteger;

	@IQColumn
	private Long myLong;

	@IQColumn
	private Float myFloat;

	@IQColumn
	private Double myDouble;

	@IQColumn
	private BigDecimal myBigDecimal;

	@IQColumn
	private String myString;

	@IQColumn
	private java.util.Date myUtilDate;

	@IQColumn
	private java.sql.Date mySqlDate;

	@IQColumn
	private java.sql.Time mySqlTime;

	@IQColumn
	private java.sql.Timestamp mySqlTimestamp;

	@IQColumn
	private byte[] myBlob;

	// test default enum type NAME
	@IQColumn(trim = true, length = 25)
	private Flower myDefaultFlower;

	@IQEnum(EnumType.NAME)
	@IQColumn(trim = true, length = 25)
	private Flower myFavoriteFlower;

	@IQEnum(EnumType.ORDINAL)
	@IQColumn
	private Flower myOtherFavoriteFlower;

	@IQEnum(EnumType.ORDINAL)
	@IQColumn
	// override the default enum strategy and use the ordinal value
	private Tree myFavoriteTree;

	// @IQEnum is set on the enumeration definition and is shared
	// by all uses of Tree as an @IQColumn
	@IQColumn
	private Tree myOtherFavoriteTree;

	public static List<SupportedTypes> createList() {
		List<SupportedTypes> list = Utils.newArrayList();
		for (int i = 0; i < 10; i++) {
			list.add(randomValue());
		}
		return list;
	}

	static SupportedTypes randomValue() {
		Random rand = new Random();
		SupportedTypes s = new SupportedTypes();
		s.myBool = new Boolean(rand.nextBoolean());
		s.myByte = new Byte((byte) rand.nextInt(Byte.MAX_VALUE));
		s.myShort = new Short((short) rand.nextInt(Short.MAX_VALUE));
		s.myInteger = new Integer(rand.nextInt());
		s.myLong = new Long(rand.nextLong());
		s.myFloat = new Float(rand.nextFloat());
		s.myDouble = new Double(rand.nextDouble());
		s.myBigDecimal = new BigDecimal(rand.nextDouble());
		s.myString = Long.toHexString(rand.nextLong());
		s.myUtilDate = new java.util.Date(rand.nextLong());
		s.mySqlDate = new java.sql.Date(rand.nextLong());
		s.mySqlTime = new java.sql.Time(rand.nextLong());
		s.mySqlTimestamp = new java.sql.Timestamp(rand.nextLong());
		s.myBlob = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		s.myDefaultFlower = Flower.DAFFODIL;
		s.myFavoriteFlower = Flower.MUM;
		s.myOtherFavoriteFlower = Flower.MARIGOLD;
		s.myFavoriteTree = Tree.BIRCH;
		s.myOtherFavoriteTree = Tree.WALNUT;
		return s;
	}

	public boolean equivalentTo(SupportedTypes s) {
		boolean same = true;
		same &= myBool.equals(s.myBool);
		same &= myByte.equals(s.myByte);
		same &= myShort.equals(s.myShort);
		same &= myInteger.equals(s.myInteger);
		same &= myLong.equals(s.myLong);
		same &= myFloat.equals(s.myFloat);
		same &= myDouble.equals(s.myDouble);
		same &= myBigDecimal.equals(s.myBigDecimal);
		same &= myUtilDate.getTime() == s.myUtilDate.getTime();
		same &= mySqlTimestamp.getTime() == s.mySqlTimestamp.getTime();
		same &= mySqlDate.toString().equals(s.mySqlDate.toString());
		same &= mySqlTime.toString().equals(s.mySqlTime.toString());
		same &= myString.equals(s.myString);
		same &= compare(myBlob, s.myBlob);
		same &= myDefaultFlower.equals(s.myDefaultFlower);
		same &= myFavoriteFlower.equals(s.myFavoriteFlower);
		same &= myOtherFavoriteFlower.equals(s.myOtherFavoriteFlower);
		same &= myFavoriteTree.equals(s.myFavoriteTree);
		same &= myOtherFavoriteTree.equals(s.myOtherFavoriteTree);
		return same;
	}

	private boolean compare(byte[] a, byte[] b) {
		if (b == null) {
			return false;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This class demonstrates the table upgrade.
	 */
	@IQTable(name = "SupportedTypes", inheritColumns = true, strictTypeMapping = true)
	@IQVersion(2)
	public static class SupportedTypes2 extends SupportedTypes {

		public SupportedTypes2() {
			// nothing to do
		}
	}
}
