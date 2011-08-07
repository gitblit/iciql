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

package com.iciql;

import com.iciql.util.Utils;

/**
 * This class provides static methods that represents common SQL functions.
 */
public class Function implements Token {

	// must be a new instance
	private static final Long COUNT_STAR = Long.valueOf(0);

	protected Object[] x;
	private String name;

	protected Function(String name, Object... x) {
		this.name = name;
		this.x = x;
	}

	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		stat.appendSQL(name).appendSQL("(");
		int i = 0;
		for (Object o : x) {
			if (i++ > 0) {
				stat.appendSQL(",");
			}
			query.appendSQL(stat, null, o);
		}
		stat.appendSQL(")");
	}

	public static Long count() {
		return COUNT_STAR;
	}

	public static Integer length(Object x) {
		return Db.registerToken(Utils.newObject(Integer.class), new Function("LENGTH", x));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Number> T sum(T x) {
		return (T) Db.registerToken(Utils.newObject(x.getClass()), new Function("SUM", x));
	}

	public static Long count(Object x) {
		return Db.registerToken(Utils.newObject(Long.class), new Function("COUNT", x));
	}

	public static Boolean isNull(Object x) {
		return Db.registerToken(Utils.newObject(Boolean.class), new Function("", x) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				query.appendSQL(stat, null, x[0]);
				stat.appendSQL(" IS NULL");
			}
		});
	}

	public static Boolean isNotNull(Object x) {
		return Db.registerToken(Utils.newObject(Boolean.class), new Function("", x) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				query.appendSQL(stat, null, x[0]);
				stat.appendSQL(" IS NOT NULL");
			}
		});
	}

	public static Boolean not(Boolean x) {
		return Db.registerToken(Utils.newObject(Boolean.class), new Function("", x) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				stat.appendSQL("NOT ");
				query.appendSQL(stat, null, x[0]);
			}
		});
	}

	public static Boolean or(Boolean... x) {
		return Db.registerToken(Utils.newObject(Boolean.class), new Function("", (Object[]) x) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				int i = 0;
				for (Object o : x) {
					if (i++ > 0) {
						stat.appendSQL(" OR ");
					}
					query.appendSQL(stat, null, o);
				}
			}
		});
	}

	public static Boolean and(Boolean... x) {
		return Db.registerToken(Utils.newObject(Boolean.class), new Function("", (Object[]) x) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				int i = 0;
				for (Object o : x) {
					if (i++ > 0) {
						stat.appendSQL(" AND ");
					}
					query.appendSQL(stat, null, o);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <X> X min(X x) {
		Class<X> clazz = (Class<X>) x.getClass();
		X o = Utils.newObject(clazz);
		return Db.registerToken(o, new Function("MIN", x));
	}

	@SuppressWarnings("unchecked")
	public static <X> X max(X x) {
		Class<X> clazz = (Class<X>) x.getClass();
		X o = Utils.newObject(clazz);
		return Db.registerToken(o, new Function("MAX", x));
	}

	public static Boolean like(String x, String pattern) {
		Boolean o = Utils.newObject(Boolean.class);
		return Db.registerToken(o, new Function("LIKE", x, pattern) {
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				stat.appendSQL("(");
				query.appendSQL(stat, null, x[0]);
				stat.appendSQL(" LIKE ");
				query.appendSQL(stat, x[0], x[1]);
				stat.appendSQL(")");
			}
		});
	}

}
