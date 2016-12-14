package net.dericbourg.db

import java.sql.Connection

import org.postgresql.ds.PGPoolingDataSource

object UsingPostgres {
  private[db] lazy val datasource = {
    val p = new PGPoolingDataSource()
    p.setDatabaseName("postgres")
    p.setUser("postgres")
    p.setPassword("postgres")

    p
  }

  def apply[R](body: Connection => R): R = {
    val connection = datasource.getConnection
    try {
      body(connection)
    }
    finally {
      connection.close()
    }
  }
}
