/*
 * Copyright 2012 James Moger.
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

public class SubQuery<T, Z> {

    final Query<T> query;
    final Z z;

    public SubQuery(Query<T> query, Z x) {
        this.query = query;
        this.z = x;
    }

    public void appendSQL(SQLStatement stat) {
        stat.appendSQL(query.toSubQuery(z));
    }
}
