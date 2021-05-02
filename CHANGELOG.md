## Changelog
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [2.2.1] - 2021-05-02

#### Fixed

- Fix Oracle model generation #35
- Improve simple JOIN #36
- Several Dependabot updates

### [2.2.0] - 2017-01-31
#### Added
- Added bitwise AND and XOR DSL for where clauses
- Update to MySQL 5.7

### [2.1.1] - 2016-04-07
#### Added
- Add explicit `where(String)` method to help with non-Java language interop

### [2.1.0] - 2016-04-06
#### Added
- Add methods to select counts of a group by field (*SELECT field, COUNT(*) FROM table [WHERE conditions] GROUP BY field*)

### [2.0.0] - 2016-04-04

The groupId of the Maven dependency has changed to `com.gitblit.iciql`.
Distributing binaries through Maven Central.

#### Fixed
- Fixed order by descending on primitives (issue-21)
- Fixed setting null column values (issue-22)
- Fixed Postgres 9.5 Upsert syntax (issue-23)

#### Added
- Added groupBy methods to QueryWhere (issue-20)
- Allow selecting a column from a table

### [1.6.5] - 2015-07-21
#### Fixed
- Strip column identifiers from specified column names when mapping JDBC ResultSets to fields

### [1.6.4] - 2015-06-30
#### Added
- Support Set, List, and Map types for columns when used with DataTypeAdapter

### [1.6.3] - 2015-06-29
#### Fixed
- Fix column mapping on wildcard select JOIN statements
#### Changed
- Add support for the upcoming PostgreSQL 9.5 UPSERT syntax

### [1.6.2] - 2015-05-11
#### Fixed
- Reverted change which keyed DataTypeAdapters by the target Java type

### [1.6.1] - 2015-05-11
Respin of 1.6.0

### [1.6.0] - 2015-05-11
#### Fixed
- Fix column inheritance from superclasses (pr-14)
- Use webapp classloader rather than global classloader (pr-12)
- Fix deserialization of null values

#### Changed
- Improve SQLite dialect based on upstream JDBC improvements
- Key DataTypeAdapters by target Java type
- Drop precision and length from SQL->Java type determination
- Improve readability of the generated performance benchmark table
- Added Derby TCP benchmark test

### [1.5.0] - 2014-11-10
#### Fixed
- Fix SQLite INTEGER PRIMARY KEY AUTOINCREMENT mapping

#### Changed
- Improved automatic date conversions
- Revised data type adapters to be specified separately with the @TypeAdapter annotation

#### Added
- Add runtime mode support (DEV, TEST, & PROD)
- Add a DAO feature similar to JDBI
- Added Gson, XStream, and SnakeYaml type adapters

### [1.4.0] - 2014-11-05
#### Changed
- Table constraints are now defined as part of CREATE TABLE instead of afterwards with indivudual ALTER TABLE statements. This is more portable across database backends.

#### Added
- Support for specifying custom data type adapters in @IQColumn and Define.typeAdapter()
- Added com.iciql.SQLDialectPostgreSQL.JsonStringAdapter
- Added com.iciql.SQLDialectPostgreSQL.XmlStringAdapter
- Added com.iciql.JavaSerializationTypeAdapter to (de)serialize objects into a BLOB column
- Added an SQLite dialect

### [1.3.0] - 2014-10-22

If you are upgrading and using EnumId mapping you will have to update your enums to define the target class for the EnumId interface since it is now generified.

Switching to using generic types with the EnumId interface allows you to implement alternative enum-type mappings which may make more sense for your business logic.

#### Fixed
- Return null NPE in selectFirst() if list is empty (pr-5)
- Fix Moxie toolkit download URL (pr-6)
- Be more careful with primitive numeric type rollovers (pr-6)

#### Changed
- Revised EnumId interface to support generic types (pr-6)
#### Added
- Add syntax oneOf/noneOf for IN and NOT IN (pr-7)
- Add support for compound nested AND/OR conditions (pr-8)
- Add support for mapping SQL BOOLEAN to primitive numeric types, not just object numeric types
- Add support for customizing EnumId mapping, you can now map enum constants to values of your choice as long as they are a standard type

#### Removed

### [1.2.0] - 2013-03-25
#### Fixed
- Fixed case-sensitivity bug on setting a compound primary key from an annotation (issue 12)
- Fixed order of DEFAULT value in create table statement (issue 11)
- Fixed password bug in model generator (issue 7)

#### Changed
- Support inheriting columns from super.super class, if super.super is annotated.
  This allows for an inheritance hierarchy like:
  @IQTable class MyTable -> @IQView abstract class MyBaseView -> @IQView class MyConstrainedView

#### Added
- Implemented readonly view support. (issue 8)
  View models may be specified using the IQView annotation or Iciql.define().  Views can either be created automatically as part of a query of the view OR views may be constructed from a fluent statement.
- Support inheritance of IQVersion for DbUpgrader implementations (issue 10)
- Added @IQConstraintForeignKey annotation (issue 13)
- Added MS SQL Server dialect (issue 14)

### [1.1.0] - 2012-08-20
#### Changed
- All bulk operations (insert all, update all, delete all) now use JDBC savepoints to ensure atomicity of the transaction

### [1.0.0] - 2012-07-14
#### Fixed
- Fixed bug in using 0L primitive values in where clauses.  These were confused with the COUNT(*) function. (Github/kc5nra,issue 5)

#### Changed
- Issue CREATE TABLE and CREATE INDEX statements once per-db instance/table-mapping

#### Added
- Added support for single column subquery
  select name, address from user_table where user_id in (select user_id from invoice table where paid = false)
- Added support for left outer join (Github/backpaper0)

### [0.7.10] - 2012-01-27
#### Fixed
- Fixed default String value bug where a default empty string threw an IndexOutOfBounds exception

### [0.7.9] - 2012-01-24
#### Added
 - Added toParameter() option for SET commands and allow generating parameterized UPDATE statements
 
```java
String q = db.from(t).set(t.timestamp).toParameter().where(t.id).is(5).toSQL();
db.executeUpdate(q, new Date());
```

### [0.7.8] - 2012-01-11
#### Fixed
- Replaced non-threadsafe counter used for assigning AS identifiers in JOIN statements with an AtomicInteger
- Prevent negative rollover of the AS counter
- Fixed bug in Query.select(Z z) which assumed that Z must always be an anonymous inner class which may not always be true.  This allows for specifying an existing alias to force table or identifier usage in the generated select list.  This is very useful for DISTINCT JOIN statements where only the columns of the primary table are of interest.

#### Added
- Added optional alias parameter to Query.toSQL and QueryWhere.toSQL to force generated statement to prefix an AS identifier or, alternatively, the tablename.  
- Query.toSQL(boolean distinct, K alias)
- QueryWhere.toSQL(boolean distinct, K alias)

### [0.7.7] - 2012-01-05
#### Changed
- Disallow declaring and explicitly referencing multiple instances of an enum type within a single model.
  A runtime exception will be thrown if an attempt to use where/set/on/and/or/groupBy/orderBy(enum) and your model has multiple fields of a single enum type.

#### Added
- added Query.toSQL() and QueryWhere.toSQL() methods which, when combined with the following new methods, allows for generation of a parameterized, static sql string to be reused with a dynamic query or a PreparedStatement.
- QueryCondition.isParameter()
- QueryCondition.atLeastParameter()
- QueryCondition.atMostParameter()
- QueryCondition.exceedsParameter()
- QueryCondition.lessThanParameter()
- QueryCondition.likeParameter()
- QueryCondition.isNotParameter()

### [0.7.6] - 2011-12-21
#### Changed
- Iciql now tries to instantiate a default value from an annotated default value IFF the field object is null, it is specified nullable = false, and a defaultValue exists.  This only applies to db.insert or db.update.

### [0.7.5] - 2011-12-12
#### Fixed
- Iciql now identifies wildcard queries and builds a dynamic column lookup.  Otherwise, the original field-position-based approach is used.  This corrects the performance regression released in 0.7.4 while still fixing the wildcard statement column mapping problem.

### [0.7.4] - 2011-12-06
#### Fixed
- Fixed JOIN ON primitives
- Fixed GROUP BY primitives
- Fixed primitive references when selecting into a custom type with primitives
- Fixed inherited JaQu bug related to model classes and wildcard queries (select *).
  Iciql maps resultset columns by the index of the model class field from a list.  This assumes that all columns in the resultset have a corresponding model field definition.  This works fine for most queries because iciql explicitly selects columns from the table (select alpha, beta...) when you execute select().
  The problem is when iciql issues a dynamic wildcard query and your model does not represent all columns in the resultset: columns and fields may fail to correctly line-up.
  Iciql now maps all fields by their column name, not by their position.

#### Changed
- Disallow declaring and explicitly referencing multiple primitive booleans in a single model.  A runtime exception will be thrown if an attempt to use where/set/on/and/or/groupBy/orderBy(boolean) and your model has multiple mapped primitive boolean fields.

#### Added
- Added list alternatives to the varargs methods because it was too easy to forget list.toArray()
- Db.executeQuery(Class<? extends T> modelClass, String sql, List<?> args)
- Db.executeQuery(String sql, List<?> args)
- Query.where(String fragment, List<?> args)

### [0.7.3] - 2011-12-06
#### Fixed
- Fixed JOIN ON primitives
- Fixed GROUP BY primitives
- Fixed primitive references when selecting into a custom type with primitives

#### Changed
- Improved fluent/type-safety of joins

### [0.7.2] - 2011-11-30
#### Changed
- generated models are now serializable with a default serial version id of 1

### [0.7.1] - 2011-08-31
#### Fixed
- Fix to PostgreSQL dialect when creating autoincrement columns
- Fix to default dialect when creating autoincrement columns
     
#### Changed
- Undeprecated interface configuration
- Interface configuration now maps ALL fields, not just public fields
- Overhauled test suite and included more database configurations
- Documented POJO configuration option (limited subset of interface configuration)
     
#### Added
- Added @IQIgnore annotation to explicitly skip fields for interface configuration
- Created additional Define static methods to bring interface configuration to near-parity with annotation configuration
- Added Db.open(url) method

### [0.7.0] - 2011-08-17
#### Changed
- Finished MySQL dialect implementation.  MySQL 5.0.51b passes 100% of tests.
- Renamed StatementLogger to IciqlLogger
- Overhauled test suite and included more database configurations

#### Added
- Added PostgreSQL dialect.  PostgreSQL 9.0 passes all but the boolean-as-int tests.
- Added Db.dropTable(T) method
- Added IciqlLogger.warn method
- Added IciqlLogger.drop method

### [0.6.6] - 2011-08-15
#### Changed
- Disabled two concurrency unit tests since I believe they are flawed and do not yield reproducible results

#### Added
- Implemented HSQLDB MERGE syntax.  HSQL 2.2.4 fails 1 test which is bug-3390047 in HSQLDB.
- Added Derby database dialect.  Derby 10.7.1.1 and 10.8.1.2 pass 100% of tests.

### [0.6.5] - 2011-08-12
#### Fixed
- fixed failure of db.delete(PrimitiveModel) and db.update(PrimitiveModel)

### [0.6.4] - 2011-08-12
#### Fixed
- do not INSERT primitive autoIncrement fields, let database assign value

#### Changed
- @IQTable.createIfRequired -> @IQTable.create
- unspecified length String fields are now CLOB instead of TEXT.  dialects can intercept this and convert to another type. e.g. MySQL dialect can change CLOB to TEXT.
- java.lang.Boolean now maps to BOOLEAN instead of BIT
- expressions on unmapped fields will throw an IciqlException
- expressions on unsupported types will throw an IciqlException
- moved dialects back to main package
- moved create table and create index statement generation into dialects
- renamed _iq_versions table to iq_versions since leading _ character is troublesome for some databases
- @IQColumn(allowNull=true) -> @IQColumn(nullable=true)
- All Object columns are assumed NULLABLE unless explicitly set @IQColumn(nullable = false)
- All Primitive columns are assumed NOT NULLABLE unless explicitly set @IQColumn(nullable = true)
- changed @IQTable.primaryKey definition to use array of column names (@IQTable( primaryKey = {"name", "nickname"}) )
    
#### Added
- full support for primitives in all clauses
- DECIMAL(length, scale) support
- improved exception reporting by including generated statement, if available 
- improved automatic dialect determination on pooled connections
- added HSQL dialect.  HSQL fails 4 out of 50 unit tests. (2 failures are unimplemented merge, 1 is a bug-3390047 in HSQLDB, 1 is a concurreny issue)
- added untested MySQL dialect
- allow defining table create DEFAULT values from default object values (Date myDate = new Date(100, 1, 1); => CREATE TABLE ... myDate DATETIME DEFAULT '2000-02-01 00:00:00')

### [0.6.3] - 2011-08-08
#### Changed
- Moved dialects into separate package
- finished enum support (issue 4)

#### Added
- added UUID type support (H2 databases only)
- added partial primitives support (primitives may not be used for compile-time condition clauses)
- added between(A y).and(A z) condition syntax

### [0.6.2] - 2011-08-05
#### Fixed
- fix to versioning to support H2 1.3.158+
     
#### Changed
- @IQSchema(name="public") => @IQSchema("public")
- @IQDatabase(version=2) => @IQVersion(2)
- @IQTable(version=2) => @IQVersion(2)
- @IQIndex annotation simplified to be used for one index definition and expanded to specify index name
- @IQColumn(maxLength=20) => @IQColumn(length=20)
- @IQColumn(trimString=true) => @IQColumn(trim=true)}

#### Added
- added BLOB support (issue 1)
- added java.lang.Enum support (issue 2)
- allow runtime flexible mapping of BOOL columns to Integer fields
- allow runtime flexible mapping of INT columns to Boolean fields
- added @IQIndexes annotation to specify multiple IQIndex annotations

### [0.5.0] - 2011-08-03
#### Changed
- deprecated model class interface configuration
- added Db.open(Connection conn) method, changed constructor to default scope
- added Db.registerDialect static methods to register custom dialects
- added Query.where(String fragment, Object... args) method to build a runtime query fragment when compile-time queries are too strict
- added Db.executeQuery(String query, Object... args) to execute a complete sql query with optional arguments
- added Db.executeQuery(Class modelClass, String query, Object... args) to execute a complete sql query, with optional arguments, and build objects from the result
- added Db.buildObjects(Class modelClass, ResultSet rs) method to build objects from the ResultSet of a plain sql query
- added ThreadLocal<T> com.iciql.Utils.newThreadLocal(final Class<? extends T> clazz) method
- added optional console statement logger and SLF4J statement logger
- refactored dialect support
- throw IciqlException (which is a RuntimeException) instead of RuntimeException
- synchronized Db.classMap for concurrent sharing of a Db instance
- Database/table versioning uses the _iq_versions table, the _ jq_versions table, if present, is ignored
- Changed the following class names:
  org.h2.jaqu.Table => com.iciql.Iciql
  org.h2.jaqu.JQSchema => com.iciql.IQSchema
  org.h2.jaqu.JQDatabase => com.iciql.IQDatabase
  org.h2.jaqu.JQIndex => com.iciql.IQIndex
  org.h2.jaqu.JQTable => com.iciql.IQTable
  org.h2.jaqu.JQColumn => com.iciql.IQColumn
- Changed the following method names:
  org.h2.jaqu.Table.define() => com.iciql.Iciql.defineIQ()
  QueryConditon.bigger => QueryCondition.exceeds
  QueryConditon.biggerEqual => QueryCondition.atLeast
  QueryConditon.smaller => QueryCondition.lessThan
  QueryConditon.smallEqual => QueryCondition.atMost

[unreleased]: https://github.com/gitblit/iciql/compare/release-2.2.1...HEAD
[2.2.1]: https://github.com/gitblit/iciql/compare/release-2.2.1...release-2.2.0
[2.2.0]: https://github.com/gitblit/iciql/compare/release-2.2.0...release-2.1.1
[2.1.1]: https://github.com/gitblit/iciql/compare/release-2.1.0...release-2.1.1
[2.1.0]: https://github.com/gitblit/iciql/compare/release-2.0.0...release-2.1.0
[2.0.0]: https://github.com/gitblit/iciql/compare/v1.6.5...release-2.0.0
[1.6.5]: https://github.com/gitblit/iciql/compare/v1.6.4...v1.6.5
[1.6.4]: https://github.com/gitblit/iciql/compare/v1.6.3...v1.6.4
[1.6.3]: https://github.com/gitblit/iciql/compare/v1.6.2...v1.6.3
[1.6.2]: https://github.com/gitblit/iciql/compare/v1.6.1...v1.6.2
[1.6.1]: https://github.com/gitblit/iciql/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/gitblit/iciql/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/gitblit/iciql/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/gitblit/iciql/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/gitblit/iciql/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/gitblit/iciql/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/gitblit/iciql/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/gitblit/iciql/compare/v0.7.10...v1.0.0
[0.7.10]: https://github.com/gitblit/iciql/compare/v0.7.9...v0.7.10
[0.7.9]: https://github.com/gitblit/iciql/compare/v0.7.8...v0.7.9
[0.7.8]: https://github.com/gitblit/iciql/compare/v0.7.7...v0.7.8
[0.7.7]: https://github.com/gitblit/iciql/compare/v0.7.6...v0.7.7
[0.7.6]: https://github.com/gitblit/iciql/compare/v0.7.5...v0.7.6
[0.7.5]: https://github.com/gitblit/iciql/compare/v0.7.4...v0.7.5
[0.7.4]: https://github.com/gitblit/iciql/compare/v0.7.3...v0.7.4
[0.7.3]: https://github.com/gitblit/iciql/compare/v0.7.2...v0.7.3
[0.7.2]: https://github.com/gitblit/iciql/compare/v0.7.1...v0.7.2
[0.7.1]: https://github.com/gitblit/iciql/compare/v0.7.0...v0.7.1
[0.7.0]: https://github.com/gitblit/iciql/compare/v0.6.6...v0.7.0
[0.6.6]: https://github.com/gitblit/iciql/compare/v0.6.5...v0.6.6
[0.6.5]: https://github.com/gitblit/iciql/compare/v0.6.4...v0.6.5
[0.6.4]: https://github.com/gitblit/iciql/compare/v0.6.3...v0.6.4
[0.6.3]: https://github.com/gitblit/iciql/compare/v0.6.2...v0.6.3
[0.6.2]: https://github.com/gitblit/iciql/compare/v0.5.0...v0.6.2
