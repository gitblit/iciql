iciql
=================

[![Maven Central](http://img.shields.io/maven-central/v/com.gitblit.iciql/iciql.svg)](http://search.maven.org/#search|ga|1|com.gitblit.iciql)
[![Maven Central](https://img.shields.io/github/license/gitblit/iciql.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

iciql **is**...

- a model-based, database access wrapper for JDBC
- for modest database schemas and basic statement generation
- for those who want to write code, instead of SQL, using IDE completion and compile-time type-safety
- small (<250KB with debug symbols) with no runtime dependencies
- pronounced *icicle* (although it could be French: *ici ql* - here query language)
- a friendly fork of the H2 [JaQu](http://h2database.com/html/jaqu.html) project

iciql **is not**...

- a complete alternative to JDBC
- designed to compete with more powerful database query tools like [jOOQ](http://jooq.sourceforge.net) or [QueryDSL](http://source.mysema.com/display/querydsl/Querydsl)
- designed to compete with enterprise ORM tools like [Hibernate](http://www.hibernate.org) or [mybatis](http://www.mybatis.org)

Supported Databases (Unit-Tested)
-------
- [H2](http://h2database.com) 1.4
- [HSQLDB](http://hsqldb.org) 2.3
- [Derby](http://db.apache.org/derby) 10.12
- [MySQL](http://mysql.com) 5.6
- [PostgreSQL](http://postgresql.org) 9.5
- [SQLite](http://www.sqlite.org) 3.8

Support for others is possible and may only require creating a simple "dialect" class.

Downloading
-----------

As of 2.0.0 iciql is now distributed through Maven Central and it's coordinates have changed slightly.

```xml
<dependencies>
    <dependency>
        <groupId>com.gitblit.iciql</groupId>
        <artifactId>iciql</artifactId>
        <version>2.1.1</version>
    </dependency>
</dependencies>
```

Older releases are available from the [Iciql Maven Repository](http://gitblit.github.io/iciql/maven/). 

License
-------
iciql is distributed under the terms of the [Apache Software Foundation license, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).<br/>
The text of the license is included in the file LICENSE in the root of the project.

Java Runtime Requirement
-------
iciql requires a Java 6 Runtime Environment (JRE) or a Java 6 Development Kit (JDK).
 
Getting help
-------
Read the online documentation available at the [iciql website](http://iciql.com)<br/>
Issues & source code @ [GitHub](http://github.com/gitblit/iciql)

Building iciql
----------------

You may use Maven to build the project:

    mvn clean package
    
You may use Maven to run the test suite on the default database:

    mvn clean test

You may use Maven to run the test suite on all tested databases:

    mvn clean test-compile exec:exec
