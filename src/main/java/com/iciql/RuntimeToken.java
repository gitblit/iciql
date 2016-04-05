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
package com.iciql;

import com.iciql.util.StringUtils;

import java.text.MessageFormat;

/**
 * Represents a traditional PreparedStatment fragment like "id=?, name=?".
 */
public class RuntimeToken implements Token {

    final String fragment;
    final Object[] args;

    public RuntimeToken(String fragment, Object... args) {
        this.fragment = fragment;
        this.args = args == null ? new Object[0] : args;
    }

    /**
     * Append the SQL to the given statement using the given query.
     *
     * @param stat  the statement to append the SQL to
     * @param query the query to use
     */
    @Override
    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        int tokenCount = StringUtils.count('?', fragment);
        if (tokenCount != args.length) {
            throw new IciqlException(MessageFormat.format(
                    "Fragment \"{0}\" specifies {1} tokens but you supplied {2} args", fragment, tokenCount,
                    args.length));
        }
        stat.appendSQL(fragment);
        for (Object arg : args) {
            stat.addParameter(arg);
        }
    }
}
