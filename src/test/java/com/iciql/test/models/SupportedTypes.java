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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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
import com.iciql.IciqlException;
import com.iciql.test.IciqlSuite;
import com.iciql.test.models.EnumModels.Tree;
import com.iciql.util.Utils;

/**
 * A data class that contains a column for each data type.
 */
@IQTable
@IQIndexes({ @IQIndex({ "myLong", "myInteger" }), @IQIndex(type = IndexType.HASH, value = "myString") })
@IQVersion(1)
public class SupportedTypes implements Serializable {

	private static final long serialVersionUID = 1L;

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

	// scale change must match the test value scale
	@IQColumn(length = 10, scale = 5)
	private BigDecimal myBigDecimal;

	@IQColumn(length = 40, trim = true)
	public String myString;

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
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.MILLISECOND, 0);
		long now = c.getTimeInMillis();

		long oneday = 24 * 60 * 60 * 1000L;
		for (int i = 0; i < 10; i++) {
			list.add(randomValue(now - (i * oneday)));
		}
		return list;
	}

	static SupportedTypes randomValue(long time) {
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
		// scale must match annotation
		s.myBigDecimal = s.myBigDecimal.setScale(5, RoundingMode.UP);
		s.myString = Long.toHexString(rand.nextLong());
		s.myUtilDate = new java.util.Date(time);
		s.mySqlDate = new java.sql.Date(time);
		s.mySqlTime = new java.sql.Time(time);
		s.mySqlTimestamp = new java.sql.Timestamp(time);
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
		same &= same("myBool", myBool.equals(s.myBool));
		same &= same("myByte", myByte.equals(s.myByte));
		same &= same("myShort", myShort.equals(s.myShort));
		same &= same("myInteger", myInteger.equals(s.myInteger));
		same &= same("myLong", myLong.equals(s.myLong));
		same &= same("myFloat", IciqlSuite.equivalentTo(myFloat, s.myFloat));
		same &= same("myDouble", IciqlSuite.equivalentTo(myDouble, s.myDouble));

		BigDecimal bda = myBigDecimal.round(MathContext.DECIMAL32);
		BigDecimal bdb = s.myBigDecimal.round(MathContext.DECIMAL32);
		same &= same("myBigDecimal", bda.compareTo(bdb) == 0);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		same &= same("myUtilDate", df.format(myUtilDate).equals(df.format(s.myUtilDate)));
		same &= same("mySqlTimestamp", df.format(mySqlTimestamp).equals(df.format(s.mySqlTimestamp)));
		same &= same("mySqlDate", mySqlDate.toString().equals(s.mySqlDate.toString()));
		same &= same("mySqlTime", mySqlTime.toString().equals(s.mySqlTime.toString()));
		same &= same("myString", myString.equals(s.myString));
		same &= same("myBlob", Arrays.equals(myBlob, s.myBlob));
		same &= same("myDefaultFlower", myDefaultFlower.equals(s.myDefaultFlower));
		same &= same("myFavoriteFlower", myFavoriteFlower.equals(s.myFavoriteFlower));
		same &= same("myOtherFavoriteFlower", myOtherFavoriteFlower.equals(s.myOtherFavoriteFlower));
		same &= same("myFavoriteTree", myFavoriteTree.equals(s.myFavoriteTree));
		same &= same("myOtherFavoriteTree", myOtherFavoriteTree.equals(s.myOtherFavoriteTree));
		return same;
	}

	private boolean same(String field, boolean same) {
		if (!same) {
			throw new IciqlException("{0} is not the same",  field);
		}
		return same;
	}

	/**
	 * This class demonstrates the table upgrade.
	 */
	@IQTable(name = "SupportedTypes", inheritColumns = true)
	@IQVersion(2)
	public static class SupportedTypes2 extends SupportedTypes {

		public SupportedTypes2() {
			// nothing to do
		}
	}
}
