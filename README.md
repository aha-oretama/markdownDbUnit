# markdownDbUnit

This repository is markdown's extension for [DBUnit](http://dbunit.sourceforge.net/).

# Motivation

Why I made markdown's extention? It is a simple reason. Markdown has high affinity with GitHub!!  

GitHub supports markdown in high level.  
If your project has many test data's files for testing and your service's enhancement will change the files drasticly, then, you want to see the differences betweens new and old files in review of pull request. Is it convinience to see the differences as follows!?

![pullRequestDifferences](https://raw.github.com/aha-oretama/markdownDbUnit/master/images/pullRequestDifferences.png)

That's the reason!

# Required
* Java 7 or above

# Usage

## Maven
Add this repository and depenedency.  
This dependency includes DBUnit, so you can now use DBUnit.  

```xml
    <repositories>
        <repository>
            <id>markdownDbUnit</id>
            <url>https://raw.github.com/aha-oretama/markdownDbUnit/mvn-repo/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>aha-oretama.jp</groupId>
            <artifactId>markdownDbUnit</artifactId>
            <version>1.0.0-RC1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

If you want to use different version's DBUnit,
exclude dependency and add dependency as follow.

```xml
    <dependencies>
        <dependency>
            <groupId>aha-oretama.jp</groupId>
            <artifactId>markdownDbUnit</artifactId>
            <version>1.0.0-RC1</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <artifactId>org.dbunit</artifactId>
                    <groupId>dbunit</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        
        <dependency>
            <groupId>org.dbunit</groupId>
            <artifactId>dbunit</artifactId>
            <version>2.5.0</version>
        </dependency>
    </dependencies>
```

## How to use
You can use like DBUnit. The usage of DBUnit is [here](http://dbunit.sourceforge.net/howto.html).

Here is the example for clean, insert. The point is only using `MarkdownDataSet`.
```groovy
  def setup() {
    IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER, PASSWORD)
    IDatabaseConnection connection = databaseTester.getConnection()
    IDataSet dataSet = new MarkdownDataSet(new File(PATH))
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
  }
```

## MarkDown Format
Markdown's table represents a database's table and data.
A file can include multiple tables.

Support align columns feature. 

```markdown
# User
| user_id | first_name | last_name | gender | age | 
|---------| ----------- |:-------- | ----: |:---:|
| 00001 | hogehoge | taro | male | 20 |

# Score 
user_id | subject | score
------- | :-------: |:-----
00001 | Mathematics | 100

```

If columns which exit database's table are not written in markdown's table,
the columns are treated as `null`. The columns written as `null` are also treated as `null`.
Empty columns are treated as empty string. 

# Supoort 
* DBUnit 2.5.0 or above

I did not verify DBUnit version under 2.5.0, but I think it goes well.
