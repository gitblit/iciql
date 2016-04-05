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

package com.iciql.bytecode;

import com.iciql.Query;
import com.iciql.SQLStatement;

/**
 * A literal number.
 */
public class ConstantNumber implements Constant {

    private final String value;
    private final Type type;
    private final long longValue;

    private ConstantNumber(String value, long longValue, Type type) {
        this.value = value;
        this.longValue = longValue;
        this.type = type;
    }

    static ConstantNumber get(String v) {
        return new ConstantNumber(v, 0, Type.STRING);
    }

    static ConstantNumber get(int v) {
        return new ConstantNumber("" + v, v, Type.INT);
    }

    static ConstantNumber get(long v) {
        return new ConstantNumber("" + v, v, Type.LONG);
    }

    static ConstantNumber get(String s, long x, Type type) {
        return new ConstantNumber(s, x, type);
    }

    public int intValue() {
        return (int) longValue;
    }

    public String toString() {
        return value;
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        stat.appendSQL(toString());
    }

    public Constant.Type getType() {
        return type;
    }

}
