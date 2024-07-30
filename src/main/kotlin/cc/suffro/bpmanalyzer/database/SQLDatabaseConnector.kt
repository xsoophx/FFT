package cc.suffro.bpmanalyzer.database

import java.sql.Connection
import java.sql.DriverManager

class SQLDatabaseConnector(private val url: String) : DatabaseConnector {
    override fun getConnection(): Connection {
        require(url.startsWith("jdbc:sqlite")) { "Only SQLite databases are supported as database URL." }
        require(url.split("/").last().endsWith(".db")) { "Only .db files are supported." }

        return DriverManager.getConnection(url)
    }

    override fun getDatabaseNameByUrl(): String {
        return url.split("/").last().split(".").first()
    }
}
