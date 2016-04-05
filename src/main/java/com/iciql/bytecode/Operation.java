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
 * A mathematical or comparison operation.
 */
class Operation implements Token {

    /**
     * The operation type.
     */
    enum Type {
        EQUALS("=") {
            Type reverse() {
                return NOT_EQUALS;
            }
        },
        NOT_EQUALS("<>") {
            Type reverse() {
                return EQUALS;
            }
        },
        BIGGER(">") {
            Type reverse() {
                return SMALLER_EQUALS;
            }
        },
        BIGGER_EQUALS(">=") {
            Type reverse() {
                return SMALLER;
            }
        },
        SMALLER_EQUALS("<=") {
            Type reverse() {
                return BIGGER;
            }
        },
        SMALLER("<") {
            Type reverse() {
                return BIGGER_EQUALS;
            }
        },
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"), MOD("%");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        Type reverse() {
            return null;
        }

    }

    private final Token left, right;
    private final Type op;

    private Operation(Token left, Type op, Token right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    static Token get(Token left, Type op, Token right) {
        if (op == Type.NOT_EQUALS && "0".equals(right.toString())) {
            return left;
        }
        return new Operation(left, op, right);
    }

    public String toString() {
        return left + " " + op + " " + right;
    }

    public Token reverse() {
        return get(left, op.reverse(), right);
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        left.appendSQL(stat, query);
        stat.appendSQL(op.toString());
        right.appendSQL(stat, query);
    }

}
