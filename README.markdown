iciql
=================
iciql **is**...

- a model-based, database access wrapper for JDBC
- for modest database schemas and basic statement generation
- for those who want to write code, instead of SQL, using IDE completion and compile-time type-safety
- small (175KB with debug symbols) with no runtime dependencies
- pronounced *icicle* (although it could be French: *ici ql* - here query language)
- a friendly fork of the H2 [JaQu](http://h2database.com/html/jaqu.html) project

iciql **is not**...

- a complete alternative to JDBC
- designed to compete with more powerful database query tools like [jOOQ](http://jooq.sourceforge.net) or [Querydsl](http://source.mysema.com/display/querydsl/Querydsl)
- designed to compete with enterprise ORM tools like [Hibernate](http://www.hibernate.org) or [mybatis](http://www.mybatis.org)

Supported Databases (Unit-Tested)
-------
- [H2](http://h2database.com) 1.3.168
- [HSQLDB](http://hsqldb.org) 2.2.8
- [Derby](http://db.apache.org/derby) 10.9.1.0
- [MySQL](http://mysql.com) 5.0.51b
- [PostgreSQL](http://postgresql.org) 9.0

Support for others is possible and may only require creating a simple "dialect" class.

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
Issues, binaries, & sources @ [Google Code](http://code.google.com/p/iciql)

Building iciql
----------------
[Eclipse](http://eclipse.org) is recommended for development as the project settings are preconfigured.

1. Import the iciql project into your Eclipse workspace.<br/>
*There will be lots of build errors.*
2. Using Ant, execute the `build.xml` script in the project root.<br/>
*This will download all necessary build dependencies.*
3. Select your iciql project root and **Refresh** the project, this should correct all build problems.