package app.flowplay.cross.storage

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.flowplay.cross.repository.SourceRepository

fun androidSourceRepository(context: Context): SourceRepository = SourceRepository(
    AndroidSqliteDriver(FlowDatabase.Schema, context.applicationContext, "flowplay.db"),
)
