package net.dericbourg.db

import java.sql.Connection

import org.apache.commons.dbcp2._

private[db] object Datasource {
  val connectionPool = new BasicDataSource()
  connectionPool.setUrl("jdbc:postgresql://localhost:5432/postgres")
  connectionPool.setUsername("postgres")
  connectionPool.setPassword("postgres")
  connectionPool.setDriverClassName("org.postgresql.Driver")
  connectionPool.setInitialSize(3)
}

object UsingPostgres {

  def apply[R](body: Connection => R): R = {
    val connection = Datasource.connectionPool.getConnection
    try {
      body(connection)
    }
    finally {
      connection.close()
    }
  }
}
