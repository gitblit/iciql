/*
 * Copyright 2016 James Moger.
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

import java.io.Serializable;
import java.util.Date;

public class ValueCount<X> implements Serializable, Comparable<ValueCount<X>> {

    private final static long serialVersionUID = 1L;

    private final X value;
    private final long count;

    ValueCount(X x, long count) {
        this.value = x;
        this.count = count;
    }

    public X getValue() {
        return value;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return String.format("%s=%d", value, count);
    }

    @Override
    public int compareTo(ValueCount<X> o) {
        return (count < o.count) ? -1 : ((count == o.count) ? compareValue(o.value) : 1);
    }

    private int compareValue(X anotherValue) {
        if (value == null && anotherValue == null) {
            return 0;
        } else if (value == null) {
            return 1;
        } else if (anotherValue == null) {
            return -1;
        }

        if (Number.class.isAssignableFrom(value.getClass())) {
            long n1 = ((Number) value).longValue();
            long n2 = ((Number) anotherValue).longValue();
            return (n1 < n2) ? -1 : ((n1 == n2) ? 0 : 1);
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            return ((Date) value).compareTo((Date) anotherValue);
        }

        return value.toString().compareToIgnoreCase(anotherValue.toString());
    }
}

