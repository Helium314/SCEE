package de.westnordost.streetcomplete.data

import android.content.Context
import org.sqlite.database.sqlite.SQLiteOpenHelper
import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.ApplicationConstants
import javax.inject.Singleton

@Module
object DbModule {
    init {
        System.loadLibrary("sqliteX")
    }
    @Provides @Singleton fun sqLiteOpenHelper(ctx: Context): SQLiteOpenHelper =
        sqLiteOpenHelper(ctx, ApplicationConstants.DATABASE_NAME)

    fun sqLiteOpenHelper(ctx: Context, databaseName: String): SQLiteOpenHelper =
        StreetCompleteSQLiteOpenHelper(ctx, databaseName)

    @Provides @Singleton fun database(sqLiteOpenHelper: SQLiteOpenHelper): Database =
        AndroidDatabase(sqLiteOpenHelper)
}
