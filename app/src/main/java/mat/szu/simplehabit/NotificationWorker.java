package mat.szu.simplehabit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker
{
    private static final String CHANNEL_ID = "simple_habit_notification_channel";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String LAST_OPEN_DATE = "lastOpenDate";
    private final Context context;
    SharedPreferences sharedPreferences;
    
    public NotificationWorker(Context context, WorkerParameters params)
    {
        super(context, params);
        this.context      = context.getApplicationContext( );
        sharedPreferences = getApplicationContext( ).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    private static long getDelayInMillis(int hour, int minutes)
    {
        Calendar currentTime = Calendar.getInstance( );
        
        Calendar targetTime = Calendar.getInstance( );
        targetTime.set(Calendar.HOUR_OF_DAY, hour);
        targetTime.set(Calendar.MINUTE, minutes);
        targetTime.set(Calendar.SECOND, 0);
        
        long delayInMillis = targetTime.getTimeInMillis( ) - currentTime.getTimeInMillis( );
        
        if (delayInMillis < 0)
        {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
            delayInMillis = targetTime.getTimeInMillis( ) - currentTime.getTimeInMillis( );
        }
        return delayInMillis;
    }
    
    @NonNull
    @Override
    public Result doWork( )
    {
        TasksManager tasksManager = new TasksManager(context);
        StatsManager statsManager = new StatsManager(context);
        
        String  taskName    = getInputData( ).getString("title");
        String  contentText = getInputData( ).getString("content");
        int     taskId      = getInputData( ).getInt("id", -1);
        boolean isRecurring = getInputData( ).getBoolean("recurring", false);
        int     hour        = getInputData( ).getInt("hour", -1);
        int     minutes     = getInputData( ).getInt("minutes", -1);
        
        Task task = tasksManager.getTaskById(taskId);
        if (task == null)
            return Result.success( );
        
        SimpleDateFormat sdf       = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           todayDate = sdf.format(Calendar.getInstance( ).getTime( ));
        String           lastOpen  = sharedPreferences.getString(LAST_OPEN_DATE, "");
        if (task.isActive( ))
        {
            if (todayDate.equals(lastOpen) && !task.isDone( ))
                sendNotification(taskName, contentText, taskId, isRecurring);
            if (!todayDate.equals(lastOpen))
            {
                statsManager.changeCount(todayDate, 4, 1, true);
                sendNotification(taskName, contentText, taskId, isRecurring);
            }
        }
        
        if (minutes >= 0 && hour >= 0 && isRecurring && task.isActive( ))
            scheduleNotification(taskId, contentText, hour, minutes);
        return Result.success( );
    }
    
    private void scheduleNotification(int notificationId, String content, int hour, int minutes)
    {
        long delayInMillis = getDelayInMillis(hour, minutes);
        Data data =
                new Data.Builder( ).putString("title", "Zadanie zrobione?").putString("content", content).putInt("id", notificationId).putBoolean(
                        "recurring", true).putInt("hour", hour).putInt("minutes", minutes).build( );
        
        WorkRequest notificationWork =
                new OneTimeWorkRequest.Builder(NotificationWorker.class).setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS).setInputData(data).build( );
        WorkManager.getInstance(context).enqueue(notificationWork);
    }
    
    private void sendNotification(String taskName, String contentText, int taskId, boolean isRecurring)
    {
        Context context = getApplicationContext( );
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Intent notDoneButtonIntent = new Intent(context, NotificationActionReceiver.class);
        notDoneButtonIntent.setAction("Niewykonane");
        notDoneButtonIntent.putExtra("notificationID", taskId);
        notDoneButtonIntent.putExtra("recurring", isRecurring);
        PendingIntent action1PendingIntent = PendingIntent.getBroadcast(context, taskId * 2, notDoneButtonIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Intent doneButtonIntent = new Intent(context, NotificationActionReceiver.class);
        doneButtonIntent.setAction("Wykonane");
        doneButtonIntent.putExtra("notificationID", taskId);
        doneButtonIntent.putExtra("recurring", isRecurring);
        PendingIntent action2PendingIntent = PendingIntent.getBroadcast(context, taskId * 2 + 1, doneButtonIntent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(taskName).setContentText(contentText).setPriority(NotificationCompat.PRIORITY_HIGH).setDefaults(NotificationCompat.DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true).addAction(android.R.drawable.btn_plus, "Niewykonane", action1PendingIntent).addAction(android.R.drawable.btn_minus, "Wykonane", action2PendingIntent);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(taskId, builder.build( ));
    }
    
}
