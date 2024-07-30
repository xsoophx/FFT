package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SQLDatabaseConnectorTest : BaseTest() {
    @Test
    fun `should throw if no jdbc prefix`() {
        val invalidUrl = "path/to/database"
        val databaseConnector = SQLDatabaseConnector(invalidUrl)
        assertThrows<IllegalArgumentException> { databaseConnector.getConnection() }
    }

    @Test
    fun `should throw if no db suffix`() {
        val invalidUrl = "jdbc:sqlite:path/to/database"
        val databaseConnector = SQLDatabaseConnector(invalidUrl)
        assertThrows<IllegalArgumentException> { databaseConnector.getConnection() }
    }
}
