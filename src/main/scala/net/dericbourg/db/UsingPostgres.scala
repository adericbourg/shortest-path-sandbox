package net.dericbourg.db

import java.sql.Connection

import org.postgresql.ds.PGPoolingDataSource

import scala.util.Try

object UsingPostgres {
  lazy val datasource = {
    val p = new PGPoolingDataSource()
    p.setDatabaseName("postgres")
    p.setUser("postgres")
    p.setPassword("postgres")

    p
  }

  def apply[R](f: Connection => R): Try[R] = {
    tryWith(datasource.getConnection) { connection =>
      f(connection)
    }
  }
}
