package cc.suffro.bpmanalyzer.database

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val databaseModule =
    module {
        singleOf(::SQLiteDatabase) {
            bind<DatabaseOperations>()
        }
        singleOf(::SQLDatabaseConnector) {
            bind<DatabaseConnector>()
        }
    }
