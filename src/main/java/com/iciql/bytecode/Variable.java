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
import com.iciql.Token;

/**
 * A variable.
 */
public class Variable implements Token {

    static final Variable THIS = new Variable("this", null);

    private final String name;
    private final Object obj;

    private Variable(String name, Object obj) {
        this.name = name;
        this.obj = obj;
    }

    static Variable get(String name, Object obj) {
        return new Variable(name, obj);
    }

    public String toString() {
        return name;
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        query.appendSQL(stat, null, obj);
    }

}
