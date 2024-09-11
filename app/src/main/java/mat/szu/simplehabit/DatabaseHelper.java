package mat.szu.simplehabit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    // Tabela Tasks
    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_COLUMN_ID = "id";
    public static final String TASKS_COLUMN_NAME = "name";
    public static final String TASKS_COLUMN_START_DATE = "startDate";
    public static final String TASKS_COLUMN_END_DATE = "endDate";
    public static final String TASKS_COLUMN_NOTIFICATION_TIME = "notificationTime";
    public static final String TASKS_COLUMN_IS_RECURRING = "recurring";
    public static final String TASKS_COLUMN_IS_ACTIVE = "active";
    public static final String TASKS_COLUMN_IS_DONE = "done";
    public static final String TASKS_COLUMN_LAST_LAST_DONE = "lastLastDone";
    public static final String TASKS_COLUMN_LAST_DONE = "lastDone";
    // Tabela Stats
    public static final String STATS_TABLE_NAME = "stats";
    public static final String STATS_COLUMN_DAY = "day";
    public static final String STATS_COLUMN_ONCE_COUNT = "onceCount";
    public static final String STATS_COLUMN_REPEAT_COUNT = "repeatCount";
    public static final String STATS_COLUMN_ONCE_MISSES = "onceMisses";
    public static final String STATS_COLUMN_REPEAT_MISSES = "repeatMisses";
    
    private static final String DATABASE_NAME = "simpleHabit.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String CREATE_TASKS_TABLE =
            "CREATE TABLE " + TASKS_TABLE_NAME + " (" + TASKS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASKS_COLUMN_NAME + " TEXT, "
            + TASKS_COLUMN_START_DATE + " TEXT, " + TASKS_COLUMN_END_DATE + " TEXT, " + TASKS_COLUMN_NOTIFICATION_TIME + " TEXT, "
            + TASKS_COLUMN_IS_RECURRING + " INTEGER DEFAULT 0, " + TASKS_COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " + TASKS_COLUMN_IS_DONE
            + " INTEGER DEFAULT 0, " + TASKS_COLUMN_LAST_LAST_DONE + " TEXT DEFAULT '', " + TASKS_COLUMN_LAST_DONE + " TEXT DEFAULT '');";
    
    private static final String CREATE_STATS_TABLE =
            "CREATE TABLE " + STATS_TABLE_NAME + " (" + STATS_COLUMN_DAY + " TEXT PRIMARY KEY, " + STATS_COLUMN_ONCE_COUNT + " INTEGER DEFAULT 0, "
            + STATS_COLUMN_REPEAT_COUNT + " INTEGER DEFAULT 0, " + STATS_COLUMN_ONCE_MISSES + " INTEGER DEFAULT 0, " + STATS_COLUMN_REPEAT_MISSES
            + " INTEGER DEFAULT 0);";
    
    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TASKS_TABLE);
        db.execSQL(CREATE_STATS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STATS_TABLE_NAME);
        onCreate(db);
    }
    
    public boolean resetTables( )
    {
        SQLiteDatabase db = null;
        try
        {
            db = this.getWritableDatabase( );
            db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + STATS_TABLE_NAME);
            onCreate(db);
            return true;
        } catch (Exception e)
        {
            return false;
        } finally
        {
            if (db != null && db.isOpen( ))
                db.close( );
        }
    }
}
