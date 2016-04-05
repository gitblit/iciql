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

import com.iciql.Iciql;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.iciql.Define.length;
import static com.iciql.Define.primaryKey;

/**
 * A table containing all possible data types.
 */

public class ComplexObject implements Iciql {
    public Integer id;
    public Long amount;
    public String name;
    public BigDecimal value;
    public Date birthday;
    public Time time;
    public Timestamp created;

    static ComplexObject build(Integer id, boolean isNull) {
        ComplexObject obj = new ComplexObject();
        obj.id = id;
        obj.amount = isNull ? null : Long.valueOf(1);
        obj.name = isNull ? null : "hello";
        obj.value = isNull ? null : new BigDecimal("1");
        obj.birthday = isNull ? null : java.sql.Date.valueOf("2001-01-01");
        obj.time = isNull ? null : Time.valueOf("10:20:30");
        obj.created = isNull ? null : Timestamp.valueOf("2002-02-02 02:02:02");
        return obj;
    }

    public void defineIQ() {
        primaryKey(id);
        length(name, 25);
    }

    public static List<ComplexObject> getList() {
        return Arrays.asList(new ComplexObject[]{build(0, true), build(1, false)});
    }

}
