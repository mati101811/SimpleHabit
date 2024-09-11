package mat.szu.simplehabit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsManager
{
    private final SQLiteDatabase database;
    
    public StatsManager(Context context)
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase( );
    }
    
    public void changeCount(String day, int column, int value, boolean add)
    {
        String columnName = "";
        switch (column)
        {
            case 1:
                columnName = DatabaseHelper.STATS_COLUMN_ONCE_COUNT;
                break;
            case 2:
                columnName = DatabaseHelper.STATS_COLUMN_REPEAT_COUNT;
                break;
            case 3:
                columnName = DatabaseHelper.STATS_COLUMN_ONCE_MISSES;
                break;
            case 4:
                columnName = DatabaseHelper.STATS_COLUMN_REPEAT_MISSES;
                break;
        }
        
        Cursor cursor = database.query(DatabaseHelper.STATS_TABLE_NAME, new String[]{
                DatabaseHelper.STATS_COLUMN_DAY
        }, DatabaseHelper.STATS_COLUMN_DAY + " = ?", new String[]{ day }, null, null, null);
        
        ContentValues values = new ContentValues( );
        if (cursor.moveToFirst( ))
        {
            String query =
                    "UPDATE " + DatabaseHelper.STATS_TABLE_NAME + " SET " + columnName + " = " + columnName + (add ? " + ?" : " - ?") + " WHERE "
                    + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            database.execSQL(query, new Object[]{ value, day });
        }
        else
        {
            values.put(DatabaseHelper.STATS_COLUMN_DAY, day);
            values.put(DatabaseHelper.STATS_COLUMN_ONCE_COUNT, 0);
            values.put(DatabaseHelper.STATS_COLUMN_REPEAT_COUNT, 0);
            values.put(DatabaseHelper.STATS_COLUMN_ONCE_MISSES, 0);
            values.put(DatabaseHelper.STATS_COLUMN_REPEAT_MISSES, 0);
            values.put(columnName, value);
            database.insert(DatabaseHelper.STATS_TABLE_NAME, null, values);
        }
        cursor.close( );
    }
    
    public int countDoneToday( )
    {
        int count = 0;
        
        SimpleDateFormat sdf       = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           todayDate = sdf.format(new Date( ));
        
        String query = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + ") AS total_done "
                       + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE " + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
        
        Cursor cursor = database.rawQuery(query, new String[]{ todayDate });
        
        if (cursor.moveToFirst( ))
            count = cursor.getInt(cursor.getColumnIndexOrThrow("total_done"));
        
        cursor.close( );
        return count;
    }
    
    public int countNotDoneToday( )
    {
        int count = 0;
        
        SimpleDateFormat sdf       = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           todayDate = sdf.format(new Date( ));
        
        String query =
                "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS total_done "
                + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE " + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
        
        Cursor cursor = database.rawQuery(query, new String[]{ todayDate });
        
        if (cursor.moveToFirst( ))
            count = cursor.getInt(cursor.getColumnIndexOrThrow("total_done"));
        
        cursor.close( );
        return count;
    }
    
    public int countAllDone( )
    {
        int totalCount = 0;
        
        String query =
                "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + ") AS total_once, " + "SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT
                + ") AS total_repeat " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
        {
            int totalOnce   = cursor.getInt(cursor.getColumnIndexOrThrow("total_once"));
            int totalRepeat = cursor.getInt(cursor.getColumnIndexOrThrow("total_repeat"));
            totalCount = totalOnce + totalRepeat;
        }
        
        cursor.close( );
        
        return totalCount;
    }
    
    public int countAllNotDone( )
    {
        int totalCount = 0;
        
        String query =
                "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + ") AS total_once, " + "SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES
                + ") AS total_repeat " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
        {
            int totalOnce   = cursor.getInt(cursor.getColumnIndexOrThrow("total_once"));
            int totalRepeat = cursor.getInt(cursor.getColumnIndexOrThrow("total_repeat"));
            totalCount = totalOnce + totalRepeat;
        }
        
        cursor.close( );
        
        return totalCount;
    }
    
    public int countAllOnceDone( )
    {
        int totalOnce = 0;
        
        String query  = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + ") AS total_once " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
            totalOnce = cursor.getInt(cursor.getColumnIndexOrThrow("total_once"));
        
        cursor.close( );
        
        return totalOnce;
    }
    
    public int countAllOnceNotDone( )
    {
        int totalOnce = 0;
        
        String query  = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + ") AS total_once " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
            totalOnce = cursor.getInt(cursor.getColumnIndexOrThrow("total_once"));
        
        cursor.close( );
        
        return totalOnce;
    }
    
    public int countAllRepeatDone( )
    {
        int totalOnce = 0;
        
        String query  = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + ") AS total_repeat " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
            totalOnce = cursor.getInt(cursor.getColumnIndexOrThrow("total_repeat"));
        
        cursor.close( );
        
        return totalOnce;
    }
    
    public int countAllRepeatNotDone( )
    {
        int totalOnce = 0;
        
        String query  = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS total_repeat " + "FROM " + DatabaseHelper.STATS_TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
            totalOnce = cursor.getInt(cursor.getColumnIndexOrThrow("total_repeat"));
        
        cursor.close( );
        
        return totalOnce;
    }
    
    public String getWeeklyStats( )
    {
        List<String>     weekDays         = new ArrayList<>( );
        SimpleDateFormat sdf              = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar         = Calendar.getInstance( );
        int              currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        int daysToMonday = (currentDayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - currentDayOfWeek;
        calendar.add(Calendar.DAY_OF_YEAR, daysToMonday);
        
        for (int i = 0; i < 7; i++)
        {
            weekDays.add(sdf.format(calendar.getTime( )));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        StringBuilder weeklyStats = new StringBuilder( );
        
        for (String day : weekDays)
        {
            String query = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + " + "
                           + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS totalTasks, "
                           + "SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + ") AS doneTasks "
                           + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE " + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int totalTasks = 0;
            int doneTasks  = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
            }
            
            assert cursor != null;
            cursor.close( );
            
            weeklyStats.append("(").append(totalTasks).append(", ").append(doneTasks).append("),");
        }
        
        return weeklyStats.length( ) > 0 ? weeklyStats.substring(0, weeklyStats.length( ) - 1) : "";
    }
    
    public String getWeeklyOnceStats( )
    {
        List<String>     weekDays         = new ArrayList<>( );
        SimpleDateFormat sdf              = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar         = Calendar.getInstance( );
        int              currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        int daysToMonday = (currentDayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - currentDayOfWeek;
        calendar.add(Calendar.DAY_OF_YEAR, daysToMonday);
        
        for (int i = 0; i < 7; i++)
        {
            weekDays.add(sdf.format(calendar.getTime( )));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        StringBuilder weeklyStats = new StringBuilder( );
        
        for (String day : weekDays)
        {
            String query =
                    "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + ") AS totalTasks, "
                    + "SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " ) AS doneTasks " + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE "
                    + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int totalTasks = 0;
            int doneTasks  = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
            }
            
            assert cursor != null;
            cursor.close( );
            
            weeklyStats.append("(").append(totalTasks).append(", ").append(doneTasks).append("),");
        }
        
        return weeklyStats.length( ) > 0 ? weeklyStats.substring(0, weeklyStats.length( ) - 1) : "";
    }
    
    public String getWeeklyRepeatStats( )
    {
        List<String>     weekDays         = new ArrayList<>( );
        SimpleDateFormat sdf              = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar         = Calendar.getInstance( );
        int              currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        int daysToMonday = (currentDayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - currentDayOfWeek;
        calendar.add(Calendar.DAY_OF_YEAR, daysToMonday);
        
        for (int i = 0; i < 7; i++)
        {
            weekDays.add(sdf.format(calendar.getTime( )));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        StringBuilder weeklyStats = new StringBuilder( );
        
        for (String day : weekDays)
        {
            String query = "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS "
                           + "totalTasks, " + "SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + " ) AS doneTasks " + "FROM "
                           + DatabaseHelper.STATS_TABLE_NAME + " WHERE " + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int totalTasks = 0;
            int doneTasks  = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
            }
            
            assert cursor != null;
            cursor.close( );
            
            weeklyStats.append("(").append(totalTasks).append(", ").append(doneTasks).append("),");
        }
        
        return weeklyStats.length( ) > 0 ? weeklyStats.substring(0, weeklyStats.length( ) - 1) : "";
    }
    
    public List<Integer> getMonthlyStats( )
    {
        List<Integer>    monthStats  = new ArrayList<>( );
        SimpleDateFormat sdf         = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar    = Calendar.getInstance( );
        int              daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        for (int i = 0; i < daysInMonth; i++)
        {
            String day = sdf.format(calendar.getTime( ));
            String query =
                    "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + ") AS doneTasks, "
                    + "SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + " + "
                    + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS totalTasks " + "FROM "
                    + DatabaseHelper.STATS_TABLE_NAME + " WHERE " + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int doneTasks  = 0;
            int totalTasks = 0;
            int percentage = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
            }
            
            if (totalTasks > 0)
            {
                percentage = Math.round(((float) doneTasks / totalTasks) * 100);
            }
            
            assert cursor != null;
            cursor.close( );
            
            monthStats.add(percentage);
            
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return monthStats;
    }
    
    public List<Integer> getMonthlyOnceStats( )
    {
        List<Integer>    monthStats  = new ArrayList<>( );
        SimpleDateFormat sdf         = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar    = Calendar.getInstance( );
        int              daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        for (int i = 0; i < daysInMonth; i++)
        {
            String day = sdf.format(calendar.getTime( ));
            String query =
                    "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT + ") AS doneTasks, " + "SUM(" + DatabaseHelper.STATS_COLUMN_ONCE_COUNT
                    + " + " + DatabaseHelper.STATS_COLUMN_ONCE_MISSES + ") AS totalTasks " + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE "
                    + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int doneTasks  = 0;
            int totalTasks = 0;
            int percentage = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
            }
            
            if (totalTasks > 0)
                percentage = Math.round(((float) doneTasks / totalTasks) * 100);
            
            assert cursor != null;
            cursor.close( );
            
            monthStats.add(percentage);
            
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return monthStats;
    }
    
    public List<Integer> getMonthlyRepeatStats( )
    {
        List<Integer>    monthStats  = new ArrayList<>( );
        SimpleDateFormat sdf         = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        Calendar         calendar    = Calendar.getInstance( );
        int              daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        for (int i = 0; i < daysInMonth; i++)
        {
            String day = sdf.format(calendar.getTime( ));
            String query =
                    "SELECT SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT + ") AS doneTasks, " + "SUM(" + DatabaseHelper.STATS_COLUMN_REPEAT_COUNT
                    + " + " + DatabaseHelper.STATS_COLUMN_REPEAT_MISSES + ") AS totalTasks " + "FROM " + DatabaseHelper.STATS_TABLE_NAME + " WHERE "
                    + DatabaseHelper.STATS_COLUMN_DAY + " = ?";
            
            Cursor cursor = database.rawQuery(query, new String[]{ day });
            
            int doneTasks  = 0;
            int totalTasks = 0;
            int percentage = 0;
            
            if (cursor != null && cursor.moveToFirst( ))
            {
                doneTasks  = cursor.getInt(cursor.getColumnIndexOrThrow("doneTasks"));
                totalTasks = cursor.getInt(cursor.getColumnIndexOrThrow("totalTasks"));
            }
            
            if (totalTasks > 0)
                percentage = Math.round(((float) doneTasks / totalTasks) * 100);
            
            assert cursor != null;
            cursor.close( );
            
            monthStats.add(percentage);
            
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return monthStats;
    }
    
}

