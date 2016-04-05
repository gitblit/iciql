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
 * A conditional expression.
 */
public class CaseWhen implements Token {

    private final Token condition, ifTrue, ifFalse;

    private CaseWhen(Token condition, Token ifTrue, Token ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    static Token get(Token condition, Token ifTrue, Token ifFalse) {
        if ("0".equals(ifTrue.toString()) && "1".equals(ifFalse.toString())) {
            return Not.get(condition);
        } else if ("1".equals(ifTrue.toString()) && "0".equals(ifFalse.toString())) {
            return condition;
        } else if ("0".equals(ifTrue.toString())) {
            return And.get(Not.get(condition), ifFalse);
        }
        return new CaseWhen(condition, ifTrue, ifFalse);
    }

    public String toString() {
        return "CASEWHEN(" + condition + ", " + ifTrue + ", " + ifFalse + ")";
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        stat.appendSQL("CASEWHEN ");
        condition.appendSQL(stat, query);
        stat.appendSQL(" THEN ");
        ifTrue.appendSQL(stat, query);
        stat.appendSQL(" ELSE ");
        ifFalse.appendSQL(stat, query);
        stat.appendSQL(" END");
    }

}
