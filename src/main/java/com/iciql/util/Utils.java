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

package com.iciql.util;

import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.Iciql.EnumId;
import com.iciql.Iciql.EnumType;
import com.iciql.Iciql.IQEnum;
import com.iciql.Iciql.TypeAdapter;
import com.iciql.IciqlException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generic utility methods.
 */
public class Utils {

    public static final AtomicLong COUNTER = new AtomicLong(0);

    public static final AtomicInteger AS_COUNTER = new AtomicInteger(0);

    private static final boolean MAKE_ACCESSIBLE = true;

    private static final int BUFFER_BLOCK_SIZE = 4 * 1024;

    public static synchronized int nextAsCount() {
        // prevent negative values and use a threadsafe counter
        int count = AS_COUNTER.incrementAndGet();
        if (count == Integer.MAX_VALUE) {
            count = 0;
            AS_COUNTER.set(count);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public static <X> Class<X> getClass(X x) {
        return (Class<X>) x.getClass();
    }

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new IciqlException(e);
        }
    }

    public static <T> Iterable<T> newArrayIterable(final T[] a) {
        return Arrays.asList(a);
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newArrayList(Collection<T> c) {
        return new ArrayList<T>(c);
    }

    public static <T> HashSet<T> newHashSet() {
        return new HashSet<T>();
    }

    public static <T> HashSet<T> newHashSet(Collection<T> list) {
        return new HashSet<T>(list);
    }

    public static <A, B> HashMap<A, B> newHashMap() {
        return new HashMap<A, B>();
    }

    public static <A, B> Map<A, B> newSynchronizedHashMap() {
        HashMap<A, B> map = newHashMap();
        return Collections.synchronizedMap(map);
    }

    public static <A, B> IdentityHashMap<A, B> newIdentityHashMap() {
        return new IdentityHashMap<A, B>();
    }

    public static <T> ThreadLocal<T> newThreadLocal(final Class<? extends T> clazz) {
        return new ThreadLocal<T>() {
            @SuppressWarnings("rawtypes")
            @Override
            protected T initialValue() {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    if (MAKE_ACCESSIBLE) {
                        Constructor[] constructors = clazz.getDeclaredConstructors();
                        // try 0 length constructors
                        for (Constructor c : constructors) {
                            if (c.getParameterTypes().length == 0) {
                                c.setAccessible(true);
                                try {
                                    return clazz.newInstance();
                                } catch (Exception e2) {
                                    // ignore
                                }
                            }
                        }
                    }
                    throw new IciqlException(e,
                            "Missing default constructor?  Exception trying to instantiate {0}: {1}", clazz.getName(),
                            e.getMessage());
                }
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T newObject(Class<T> clazz) {
        // must create new instances
        if (clazz == int.class || clazz == Integer.class) {
            return (T) new Integer((int) (COUNTER.getAndIncrement() % Integer.MAX_VALUE));
        } else if (clazz == String.class) {
            return (T) ("" + COUNTER.getAndIncrement());
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) new Long(COUNTER.getAndIncrement());
        } else if (clazz == short.class || clazz == Short.class) {
            return (T) new Short((short) (COUNTER.getAndIncrement() % Short.MAX_VALUE));
        } else if (clazz == byte.class || clazz == Byte.class) {
            return (T) new Byte((byte) (COUNTER.getAndIncrement() % Byte.MAX_VALUE));
        } else if (clazz == float.class || clazz == Float.class) {
            return (T) new Float(COUNTER.getAndIncrement());
        } else if (clazz == double.class || clazz == Double.class) {
            return (T) new Double(COUNTER.getAndIncrement());
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            COUNTER.getAndIncrement();
            return (T) new Boolean(false);
        } else if (clazz == BigDecimal.class) {
            return (T) new BigDecimal(COUNTER.getAndIncrement());
        } else if (clazz == BigInteger.class) {
            return (T) new BigInteger("" + COUNTER.getAndIncrement());
        } else if (clazz == java.sql.Date.class) {
            return (T) new java.sql.Date(COUNTER.getAndIncrement());
        } else if (clazz == java.sql.Time.class) {
            return (T) new java.sql.Time(COUNTER.getAndIncrement());
        } else if (clazz == java.sql.Timestamp.class) {
            return (T) new java.sql.Timestamp(COUNTER.getAndIncrement());
        } else if (clazz == java.util.Date.class) {
            return (T) new java.util.Date(COUNTER.getAndIncrement());
        } else if (clazz == java.time.LocalDate.class) {
            return (T) new java.sql.Date(COUNTER.getAndIncrement()).toLocalDate();
        } else if (clazz == java.time.LocalTime.class) {
            return (T) new java.sql.Time(COUNTER.getAndIncrement()).toLocalTime();
        } else if (clazz == java.time.LocalDateTime.class) {
            return (T) new java.sql.Timestamp(COUNTER.getAndIncrement()).toLocalDateTime();
        } else if (clazz == java.time.ZonedDateTime.class) {
            return (T) new java.sql.Timestamp(COUNTER.getAndIncrement()).toInstant().atZone(java.time.ZoneId.systemDefault());
        } else if (clazz == byte[].class) {
            COUNTER.getAndIncrement();
            return (T) new byte[0];
        } else if (clazz.isEnum()) {
            COUNTER.getAndIncrement();
            // enums can not be instantiated reflectively
            // return first constant as reference
            return clazz.getEnumConstants()[0];
        } else if (clazz == java.util.UUID.class) {
            COUNTER.getAndIncrement();
            return (T) UUID.randomUUID();
        } else if (Set.class == clazz) {
            COUNTER.getAndIncrement();
            return (T) new HashSet();
        } else if (List.class == clazz) {
            COUNTER.getAndIncrement();
            return (T) new ArrayList();
        } else if (Map.class == clazz) {
            COUNTER.getAndIncrement();
            return (T) new HashMap();
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            if (MAKE_ACCESSIBLE) {
                Constructor[] constructors = clazz.getDeclaredConstructors();
                // try 0 length constructors
                for (Constructor c : constructors) {
                    if (c.getParameterTypes().length == 0) {
                        c.setAccessible(true);
                        try {
                            return clazz.newInstance();
                        } catch (Exception e2) {
                            // ignore
                        }
                    }
                }
                // try 1 length constructors
                for (Constructor c : constructors) {
                    if (c.getParameterTypes().length == 1) {
                        c.setAccessible(true);
                        try {
                            return (T) c.newInstance(new Object[1]);
                        } catch (Exception e2) {
                            // ignore
                        }
                    }
                }
            }
            throw new IciqlException(e, "Missing default constructor?! Exception trying to instantiate {0}: {1}",
                    clazz.getName(), e.getMessage());
        }
    }

    public static <T> boolean isSimpleType(Class<T> clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        } else if (clazz == String.class) {
            return true;
        }
        return false;
    }

    public static Object convert(Object o, Class<?> targetType) {
        if (o == null) {
            return null;
        }
        Class<?> currentType = o.getClass();
        if (targetType.isAssignableFrom(currentType)) {
            return o;
        }

        // convert from CLOB/TEXT/VARCHAR to String
        if (targetType == String.class) {
            if (Clob.class.isAssignableFrom(currentType)) {
                Clob c = (Clob) o;
                try {
                    Reader r = c.getCharacterStream();
                    return readStringAndClose(r, -1);
                } catch (Exception e) {
                    throw new IciqlException(e, "error converting CLOB to String: ", e.toString());
                }
            }
            return o.toString();
        }

        if (Boolean.class.isAssignableFrom(targetType) || boolean.class.isAssignableFrom(targetType)) {
            // convert from number to boolean
            if (Number.class.isAssignableFrom(currentType)) {
                Number n = (Number) o;
                return n.intValue() > 0;
            }
            // convert from string to boolean
            if (String.class.isAssignableFrom(currentType)) {
                String s = o.toString().toLowerCase();
                float f = 0f;
                try {
                    f = Float.parseFloat(s);
                } catch (Exception e) {
                }
                return f > 0 || s.equals("true") || s.equals("yes") || s.equals("y") || s.equals("on");
            }
        }

        // convert from boolean to number
        if (Boolean.class.isAssignableFrom(currentType)) {
            Boolean b = (Boolean) o;
            Integer n = b ? 1 : 0;
            if (Number.class.isAssignableFrom(targetType)) {
                return n.intValue();
            } else if (byte.class.isAssignableFrom(targetType)) {
                return n.byteValue();
            } else if (short.class.isAssignableFrom(targetType)) {
                return n.shortValue();
            } else if (int.class.isAssignableFrom(targetType)) {
                return n.intValue();
            } else if (long.class.isAssignableFrom(targetType)) {
                return n.longValue();
            } else if (float.class.isAssignableFrom(targetType)) {
                return n.floatValue();
            } else if (double.class.isAssignableFrom(targetType)) {
                return n.doubleValue();
            } else if (boolean.class.isAssignableFrom(targetType)) {
                return b.booleanValue();
            }
        }

        // convert from number to number
        if (Number.class.isAssignableFrom(currentType)) {
            Number n = (Number) o;
            if (targetType == byte.class || targetType == Byte.class) {
                return n.byteValue();
            } else if (targetType == short.class || targetType == Short.class) {
                return n.shortValue();
            } else if (targetType == int.class || targetType == Integer.class) {
                return n.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return n.longValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return n.doubleValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return n.floatValue();
            } else if (targetType == BigDecimal.class) {
                return new BigDecimal(n.doubleValue());
            } else if (targetType == java.util.Date.class) {
                return new java.util.Date(n.longValue());
            } else if (targetType == java.sql.Date.class) {
                return new java.sql.Date(n.longValue());
            } else if (targetType == java.sql.Time.class) {
                return new java.sql.Time(n.longValue());
            } else if (targetType == java.sql.Timestamp.class) {
                return new java.sql.Timestamp(n.longValue());
            } else if (targetType == java.time.LocalDateTime.class) {
                return new java.sql.Timestamp(n.longValue()).toLocalDateTime();
            } else if (targetType == java.time.ZonedDateTime.class) {
                return new java.sql.Timestamp(n.longValue()).toInstant().atZone(java.time.ZoneId.systemDefault());
            } else if (targetType == java.time.LocalDate.class) {
                return new java.sql.Date(n.longValue()).toLocalDate();
            } else if (targetType == java.time.LocalTime.class) {
                return new java.sql.Time(n.longValue()).toLocalTime();
            }
        }

        if (Date.class.isAssignableFrom(currentType)) {
            Date d = (Date) o;
            if (targetType == Date.class) {
                return o;
            } else if (targetType == java.sql.Date.class) {
                return new java.sql.Date(d.getTime());
            } else if (targetType == java.sql.Time.class) {
                return new java.sql.Time(d.getTime());
            } else if (targetType == java.sql.Timestamp.class) {
                return new java.sql.Timestamp(d.getTime());
            } else if (targetType == java.time.LocalDateTime.class) {
                return new java.sql.Timestamp(d.getTime()).toLocalDateTime();
            } else if (targetType == java.time.ZonedDateTime.class) {
                return d.toInstant().atZone(java.time.ZoneId.systemDefault());
            } else if (targetType == java.time.LocalDate.class) {
                return new java.sql.Date(d.getTime()).toLocalDate();
            } else if (targetType == java.time.LocalTime.class) {
                return new java.sql.Time(d.getTime()).toLocalTime();
            }
        }

        // convert from BLOB
        if (targetType == byte[].class) {
            if (Blob.class.isAssignableFrom(currentType)) {
                Blob b = (Blob) o;
                try {
                    InputStream is = b.getBinaryStream();
                    return readBlobAndClose(is, -1);
                } catch (Exception e) {
                    throw new IciqlException(e, "error converting BLOB to byte[]: ", e.toString());
                }
            }
        }
        throw new IciqlException("Can not convert the value {0} from {1} to {2}", o, currentType, targetType);
    }

    /**
     * Identify the EnumType for the field.
     *
     * @param f
     * @return null or the EnumType
     */
    public static EnumType getEnumType(Field f) {
        EnumType enumType = null;
        if (f.getType().isEnum()) {
            enumType = EnumType.DEFAULT_TYPE;
            if (f.getType().isAnnotationPresent(IQEnum.class)) {
                // enum definition is annotated for all instances
                IQEnum iqenum = f.getType().getAnnotation(IQEnum.class);
                enumType = iqenum.value();
            }
            if (f.isAnnotationPresent(IQEnum.class)) {
                // this instance of the enum is annotated
                IQEnum iqenum = f.getAnnotation(IQEnum.class);
                enumType = iqenum.value();
            }
        }
        return enumType;
    }

    /**
     * Identify the EnumType from the annotations.
     *
     * @param annotations
     * @return null or the EnumType
     */
    public static EnumType getEnumType(Annotation[] annotations) {
        EnumType enumType = null;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof IQEnum) {
                    enumType = ((IQEnum) annotation).value();
                    break;
                }
            }
        }
        return enumType;
    }

    public static Class<?> getEnumTypeClass(Field f) {
        if (f.getType().isEnum()) {
            if (EnumId.class.isAssignableFrom(f.getType())) {
                // custom enumid mapping
                return ((EnumId<?>) f.getType().getEnumConstants()[0]).enumIdClass();
            }
        }
        return null;
    }

    public static Object convertEnum(Enum<?> o, EnumType type) {
        if (o == null) {
            return null;
        }
        switch (type) {
            case ORDINAL:
                return o.ordinal();
            case ENUMID:
                if (!EnumId.class.isAssignableFrom(o.getClass())) {
                    throw new IciqlException("Can not convert the enum {0} using ENUMID", o);
                }
                EnumId<?> enumid = (EnumId<?>) o;
                return enumid.enumId();
            case NAME:
            default:
                return o.name();
        }
    }

    public static Object convertEnum(Object o, Class<?> targetType, EnumType type) {
        if (o == null) {
            return null;
        }
        Class<?> currentType = o.getClass();
        if (targetType.isAssignableFrom(currentType)) {
            return o;
        }
        // convert from VARCHAR/TEXT/INT to Enum
        Enum<?>[] values = (Enum[]) targetType.getEnumConstants();
        if (Clob.class.isAssignableFrom(currentType)) {
            // TEXT/CLOB field
            Clob c = (Clob) o;
            String name = null;
            try {
                Reader r = c.getCharacterStream();
                name = readStringAndClose(r, -1);
            } catch (Exception e) {
                throw new IciqlException(e, "error converting CLOB to String: ", e.toString());
            }

            // find name match
            if (type.equals(EnumType.ENUMID)) {
                // ENUMID mapping
                for (Enum<?> value : values) {
                    EnumId<?> enumid = (EnumId<?>) value;
                    if (enumid.enumId().equals(name)) {
                        return value;
                    }
                }
            } else if (type.equals(EnumType.NAME)) {
                // standard Enum.name() mapping
                for (Enum<?> value : values) {
                    if (value.name().equalsIgnoreCase(name)) {
                        return value;
                    }
                }
            }
        } else if (String.class.isAssignableFrom(currentType)) {
            // VARCHAR field
            String name = (String) o;
            if (type.equals(EnumType.ENUMID)) {
                // ENUMID mapping
                for (Enum<?> value : values) {
                    EnumId<?> enumid = (EnumId<?>) value;
                    if (enumid.enumId().equals(name)) {
                        return value;
                    }
                }
            } else if (type.equals(EnumType.NAME)) {
                // standard Enum.name() mapping
                for (Enum<?> value : values) {
                    if (value.name().equalsIgnoreCase(name)) {
                        return value;
                    }
                }
            }
        } else if (Number.class.isAssignableFrom(currentType)) {
            // INT field
            int n = ((Number) o).intValue();
            if (type.equals(EnumType.ORDINAL)) {
                // ORDINAL mapping
                for (Enum<?> value : values) {
                    if (value.ordinal() == n) {
                        return value;
                    }
                }
            } else if (type.equals(EnumType.ENUMID)) {
                if (!EnumId.class.isAssignableFrom(targetType)) {
                    throw new IciqlException("Can not convert the value {0} from {1} to {2} using ENUMID", o,
                            currentType, targetType);
                }
                // ENUMID mapping
                for (Enum<?> value : values) {
                    EnumId<?> enumid = (EnumId<?>) value;
                    if (enumid.enumId().equals(n)) {
                        return value;
                    }
                }
            }
        } else {
            // custom object mapping
            if (type.equals(EnumType.ENUMID)) {
                if (!EnumId.class.isAssignableFrom(targetType)) {
                    throw new IciqlException("Can not convert the value {0} from {1} to {2} using ENUMID", o,
                            currentType, targetType);
                }
                // ENUMID mapping
                for (Enum<?> value : values) {
                    EnumId<?> enumid = (EnumId<?>) value;
                    if (enumid.enumId().equals(o)) {
                        return value;
                    }
                }
            }
        }
        throw new IciqlException("Can not convert the value {0} from {1} to {2}", o, currentType, targetType);
    }

    /**
     * Read a number of characters from a reader and close it.
     *
     * @param in     the reader
     * @param length the maximum number of characters to read, or -1 to read until
     *               the end of file
     * @return the string read
     */
    public static String readStringAndClose(Reader in, int length) throws IOException {
        try {
            if (length <= 0) {
                length = Integer.MAX_VALUE;
            }
            int block = Math.min(BUFFER_BLOCK_SIZE, length);
            StringWriter out = new StringWriter(length == Integer.MAX_VALUE ? block : length);
            char[] buff = new char[block];
            while (length > 0) {
                int len = Math.min(block, length);
                len = in.read(buff, 0, len);
                if (len < 0) {
                    break;
                }
                out.write(buff, 0, len);
                length -= len;
            }
            return out.toString();
        } finally {
            in.close();
        }
    }

    /**
     * Read a number of bytes from a stream and close it.
     *
     * @param in     the stream
     * @param length the maximum number of bytes to read, or -1 to read until the
     *               end of file
     * @return the string read
     */
    public static byte[] readBlobAndClose(InputStream in, int length) throws IOException {
        try {
            if (length <= 0) {
                length = Integer.MAX_VALUE;
            }
            int block = Math.min(BUFFER_BLOCK_SIZE, length);
            ByteArrayOutputStream out = new ByteArrayOutputStream(length == Integer.MAX_VALUE ? block : length);
            byte[] buff = new byte[block];
            while (length > 0) {
                int len = Math.min(block, length);
                len = in.read(buff, 0, len);
                if (len < 0) {
                    break;
                }
                out.write(buff, 0, len);
                length -= len;
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    /**
     * Identify the data type adapter class in the annotations.
     *
     * @param annotations
     * @return null or the data type adapter class
     */
    public static Class<? extends DataTypeAdapter<?>> getDataTypeAdapter(Annotation[] annotations) {
        Class<? extends DataTypeAdapter<?>> typeAdapter = null;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof TypeAdapter) {
                    typeAdapter = ((TypeAdapter) annotation).value();
                } else if (annotation.annotationType().isAnnotationPresent(TypeAdapter.class)) {
                    typeAdapter = annotation.annotationType().getAnnotation(TypeAdapter.class).value();
                }
            }
        }
        return typeAdapter;
    }
}
