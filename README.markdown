iciql
=================
iciql **is**...

- a model-based, database access wrapper for JDBC
- for modest database schemas and basic statement generation
- for those who want to write code, instead of SQL, using IDE completion and compile-time type-safety
- small (100KB) with no runtime dependencies
- pronounced *icicle* (although it could be French: *ici ql* - here query language)
- a friendly fork of the H2 [JaQu][jaqu] project

iciql **is not**...

- a complete alternative to JDBC
- designed to compete with more powerful database query tools like [jOOQ][jooq] or [Querydsl][querydsl]
- designed to compete with enterprise [ORM][orm] tools like [Hibernate][hibernate] or [mybatis][mybatis]

Supported Databases
=================
- [H2](http://h2database.com)
- Support for others is planned and should only require creating a simple "dialect" class.

Java Runtime Requirement
=================
iciql requires a Java 6 Runtime Environment (JRE) or a Java 6 Development Kit (JDK).
 
Current Release
=================
documentation @ [iciql.com](http://iciql.com)
issues, binaries, & source @ [Google Code][googlecode]<br/>
sources @ [Github][iciqlsrc]

License
=================
iciql is distributed under the terms of the [Apache Software Foundation license, version 2.0][apachelicense]

[jaqu]: http://h2database.com/html/jaqu.html "H2 JaQu project"
[orm]: http://en.wikipedia.org/wiki/Object-relational_mapping "Object Relational Mapping"
[jooq]: http://jooq.sourceforge.net "jOOQ"
[querydsl]: http://source.mysema.com/display/querydsl/Querydsl "Querydsl"
[hibernate]: http://www.hibernate.org "Hibernate"
[mybatis]: http://www.mybatis.org "mybatis"
[iciqlsrc]: http://github.com/gitblit/iciql "iciql git repository"
[googlecode]: http://code.google.com/p/iciql "iciql project management"
[apachelicense]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License, Version 2.0"