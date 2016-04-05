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
 * This class represents an incomplete condition.
 *
 * @param <A> the incomplete condition data type
 */

public class TestCondition<A> {

    private A x;

    public TestCondition(A x) {
        this.x = x;
    }

    public Boolean is(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, null, x[0]);
                stat.appendSQL(" = ");
                query.appendSQL(stat, x[0], x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean exceeds(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function(">", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, null, x[0]);
                stat.appendSQL(" > ");
                query.appendSQL(stat, x[0], x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean atLeast(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function(">=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, null, x[0]);
                stat.appendSQL(" >= ");
                query.appendSQL(stat, x[0], x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean lessThan(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("<", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, null, x[0]);
                stat.appendSQL(" < ");
                query.appendSQL(stat, x[0], x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean atMost(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("<=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, null, x[0]);
                stat.appendSQL(" <= ");
                query.appendSQL(stat, x[0], x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean like(A pattern) {
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
