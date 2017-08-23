package aha.oretama.jp

import org.dbunit.IDatabaseTester
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.operation.DatabaseOperation
import org.h2.Driver
import org.h2.tools.RunScript
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * @author aha-oretama
 */
@Unroll
class MarkdownDataSetTest extends Specification {

  private static final String JDBC_DRIVER = Driver.class.getName()
  private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  private static final String USER = "sa"
  private static final String PASSWORD = ""
  private IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER, PASSWORD)

  private static
  final String USER_SQL = "SELECT user_id,first_name,last_name,sex,age FROM user where user_id = ?"
  private static final String SCORE_SQL = "SELECT user_id,subject,score FROM score WHERE user_id = ?"

  void setupSpec() {
    // 空カラム許容
    RunScript.execute(JDBC_URL, USER, PASSWORD, "src/test/resources/testdata/schema.sql", Charset.defaultCharset(), false)
  }

  def "#test"() {
    when:
    cleanInsert("src/test/resources/testdata/${testData}")

    then:
    shouldMatchDatabaseUserColumnToExpectedValues(userId, firstName, lastName, sex, age)

    where:
    test                                                                                    | testData                 | userId  | firstName  | lastName | sex    | age
    "Inserted data have all column."                                                        | 'baseData.md'            | '00001' | 'hogehoge' | 'taro'   | 'male' | 20
    "Lacked column will be null or default value, integer is 0"                             | 'lackedColumnData.md'    | '00001' | 'hogehoge' | null     | 'male' | 0
    "Collapsed format data have save result as formatted data"                              | 'collapsedFormatData.md' | '00001' | 'hogehoge' | 'taro'   | 'male' | 20
    "Empty column will be empty character"                                                  | 'emptyColumnData.md'     | '00001' | 'hogehoge' | ''       | ''     | 20
    "Null column will be null ,integer is 0, but column for default value must be not null" | 'nullColumnData.md'      | '00001' | 'hogehoge' | null     | 'male' | 0
  }

  def "multiple tables can insert at same time"() {
    when:
    cleanInsert("src/test/resources/testdata/multiData.md")

    then:
    shouldMatchDatabaseUserColumnToExpectedValues('00001', 'hogehoge', 'taro', 'male', 20)
    shouldMatchDatabaseScoreColumnToExpectedValues('00001', 'Mathematics', 100)
  }

  private void shouldMatchDatabaseScoreColumnToExpectedValues(String userId, String subject, int score) {
    Connection connection = databaseTester.getConnection().getConnection()
    PreparedStatement userStatement = connection.prepareStatement(SCORE_SQL)
    userStatement.setString(1, userId)
    ResultSet resultSet = userStatement.executeQuery()

    assert resultSet.next()
    assert userId == resultSet.getString("user_id")
    assert subject == resultSet.getString("subject")
    assert score == resultSet.getInt("score")
  }

  private void shouldMatchDatabaseUserColumnToExpectedValues(String userId, String firstName, String lastName, String sex, Integer age) {
    Connection connection = databaseTester.getConnection().getConnection()
    PreparedStatement userStatement = connection.prepareStatement(USER_SQL)
    userStatement.setString(1, userId)
    ResultSet resultSet = userStatement.executeQuery()

    assert resultSet.next()
    assert userId == resultSet.getString("user_id")
    assert firstName == resultSet.getString("first_name")
    assert lastName == resultSet.getString("last_name")
    assert sex == resultSet.getString("sex")
    assert age == resultSet.getInt("age")
  }

  private void cleanInsert(String path) throws Exception {
    IDataSet dataSet = new MarkdownDataSet(new File(path))

    // To avoid exception when column value is empty
    IDatabaseConnection connection = databaseTester.getConnection()
    DatabaseConfig config = connection.getConfig()
    config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true)

    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
  }
}
