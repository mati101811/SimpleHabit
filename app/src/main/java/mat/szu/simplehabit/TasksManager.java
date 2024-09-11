package mat.szu.simplehabit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TasksManager
{
    private final SQLiteDatabase database;
    
    public TasksManager(Context context)
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase( );
    }
    
    public List<Task> getAllActiveTasks( )
    {
        List<Task> taskList = new ArrayList<>( );
        
        String query  = "SELECT * FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE " + DatabaseHelper.TASKS_COLUMN_IS_ACTIVE + " = 1";
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
        {
            do
            {
                long    id               = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_ID));
                String  taskName         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NAME));
                String  startDate        = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_START_DATE));
                String  endDate          = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_END_DATE));
                String  notificationTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NOTIFICATION_TIME));
                String  lastLastDone     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE));
                String  lastDone         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_DONE));
                boolean isRecurring      = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_RECURRING)) == 1;
                boolean isActive         = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE)) == 1;
                boolean isDone           = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_DONE)) == 1;
                
                Task task = new Task(id, taskName, startDate, endDate, notificationTime, isRecurring, isActive, isDone, lastLastDone, lastDone);
                taskList.add(task);
            } while (cursor.moveToNext( ));
        }
        
        cursor.close( );
        
        return taskList;
    }
    
    public Task getTaskById(int id)
    {
        Task task = null;
        
        String query  = "SELECT * FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE " + DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{ String.valueOf(id) });
        
        if (cursor != null && cursor.moveToFirst( ))
        {
            String  taskName         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NAME));
            String  startDate        = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_START_DATE));
            String  endDate          = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_END_DATE));
            String  notificationTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NOTIFICATION_TIME));
            String  lastLastDone     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE));
            String  lastDone         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_DONE));
            boolean isRecurring      = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_RECURRING)) == 1;
            boolean isActive         = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE)) == 1;
            boolean isDone           = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_DONE)) == 1;
            
            task = new Task(id, taskName, startDate, endDate, notificationTime, isRecurring, isActive, isDone, lastLastDone, lastDone);
        }
        
        assert cursor != null;
        cursor.close( );
        
        return task;
    }
    
    public void deactivateNonRecurringTasks( )
    {
        ContentValues values = new ContentValues( );
        values.put(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE, false);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_IS_RECURRING + " = ?";
        String[] whereArgs   = new String[]{ "0" };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, values, whereClause, whereArgs);
    }
    
    public List<Task> getUnfinishedTasks(String startDate)
    {
        List<Task> taskList = new ArrayList<>( );
        
        String query = "SELECT * FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE " + DatabaseHelper.TASKS_COLUMN_IS_DONE + " = 0 AND " + "(("
                       + DatabaseHelper.TASKS_COLUMN_IS_RECURRING + " = 1 AND " + DatabaseHelper.TASKS_COLUMN_IS_ACTIVE + " = 1) OR " + "("
                       + DatabaseHelper.TASKS_COLUMN_IS_RECURRING + " = 0 AND " + DatabaseHelper.TASKS_COLUMN_START_DATE + " = ?))";
        
        Cursor cursor = database.rawQuery(query, new String[]{ startDate });
        
        if (cursor.moveToFirst( ))
        {
            do
            {
                long    id               = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_ID));
                String  taskName         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NAME));
                String  start            = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_START_DATE));
                String  end              = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_END_DATE));
                String  notificationTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_NOTIFICATION_TIME));
                String  lastLastDone     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE));
                String  lastDone         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_DONE));
                boolean isRecurring      = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_RECURRING)) == 1;
                boolean isActive         = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE)) == 1;
                boolean isDone           = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_IS_DONE)) == 0;
                
                Task task = new Task(id, taskName, start, end, notificationTime, isRecurring, isActive, isDone, lastLastDone, lastDone);
                taskList.add(task);
            } while (cursor.moveToNext( ));
        }
        
        cursor.close( );
        return taskList;
    }
    
    public void resetActiveTaskStatus( )
    {
        ContentValues contentValues = new ContentValues( );
        contentValues.put(DatabaseHelper.TASKS_COLUMN_IS_DONE, 0);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_IS_ACTIVE + " = ? AND " + DatabaseHelper.TASKS_COLUMN_IS_RECURRING + " = ?";
        String[] whereArgs   = new String[]{ "1", "1" };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
    }
    
    public void setLastDoneDate(int taskID, String lastDone)
    {
        String currentLastDone = null;
        String query = "SELECT " + DatabaseHelper.TASKS_COLUMN_LAST_DONE + " FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE "
                       + DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        
        Cursor cursor = database.rawQuery(query, new String[]{ String.valueOf(taskID) });
        
        if (cursor.moveToFirst( ))
            currentLastDone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_DONE));
        cursor.close( );
        
        ContentValues contentValues = new ContentValues( );
        contentValues.put(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE, currentLastDone);
        contentValues.put(DatabaseHelper.TASKS_COLUMN_LAST_DONE, lastDone);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        String[] whereArgs   = new String[]{ String.valueOf(taskID) };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
    }
    
    public void undoLastDoneDate(int taskID)
    {
        String lastLastDone = null;
        String query = "SELECT " + DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE + " FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE "
                       + DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        
        Cursor cursor = database.rawQuery(query, new String[]{ String.valueOf(taskID) });
        
        if (cursor.moveToFirst( ))
            lastLastDone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE));
        cursor.close( );
        
        ContentValues contentValues = new ContentValues( );
        contentValues.put(DatabaseHelper.TASKS_COLUMN_LAST_DONE, lastLastDone);
        contentValues.put(DatabaseHelper.TASKS_COLUMN_LAST_LAST_DONE, "");
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        String[] whereArgs   = new String[]{ String.valueOf(taskID) };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
    }
    
    public int addTask(String taskName, String startDate, String endDate, String notificationTime, boolean isRecurring, boolean isActive,
                       boolean isDone)
    {
        ContentValues values = new ContentValues( );
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, taskName);
        values.put(DatabaseHelper.TASKS_COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.TASKS_COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.TASKS_COLUMN_NOTIFICATION_TIME, notificationTime);
        values.put(DatabaseHelper.TASKS_COLUMN_IS_RECURRING, isRecurring ? 1 : 0);
        values.put(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE, isActive ? 1 : 0);
        values.put(DatabaseHelper.TASKS_COLUMN_IS_DONE, isDone ? 1 : 0);
        
        long result = database.insert(DatabaseHelper.TASKS_TABLE_NAME, null, values);
        
        return (int) result;
    }
    
    public void setTaskStatus(long taskId, boolean isDone)
    {
        int doneValue = isDone ? 1 : 0;
        
        ContentValues contentValues = new ContentValues( );
        contentValues.put(DatabaseHelper.TASKS_COLUMN_IS_DONE, doneValue);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        String[] whereArgs   = new String[]{ String.valueOf(taskId) };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
    }
    
    public void updateTaskEndDate(long taskId, String newEndDate)
    {
        ContentValues values = new ContentValues( );
        values.put(DatabaseHelper.TASKS_COLUMN_END_DATE, newEndDate);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        String[] whereArgs   = new String[]{ String.valueOf(taskId) };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, values, whereClause, whereArgs);
    }
    
    public void deactivateTask(long taskId)
    {
        ContentValues contentValues = new ContentValues( );
        contentValues.put(DatabaseHelper.TASKS_COLUMN_IS_ACTIVE, 0);
        
        String   whereClause = DatabaseHelper.TASKS_COLUMN_ID + " = ?";
        String[] whereArgs   = new String[]{ String.valueOf(taskId) };
        
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
    }
    
    public int getActiveRepeatTasksCount( )
    {
        int count = 0;
        
        String query =
                "SELECT COUNT(*) FROM " + DatabaseHelper.TASKS_TABLE_NAME + " WHERE " + DatabaseHelper.TASKS_COLUMN_IS_ACTIVE + " = 1" + " AND "
                + DatabaseHelper.TASKS_COLUMN_IS_RECURRING + " = 1";
        
        Cursor cursor = database.rawQuery(query, null);
        
        if (cursor.moveToFirst( ))
            count = cursor.getInt(0);
        
        cursor.close( );
        
        return count;
    }
    
}
