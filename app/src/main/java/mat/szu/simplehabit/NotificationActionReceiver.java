package mat.szu.simplehabit;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationActionReceiver extends BroadcastReceiver
{
    private TasksManager tasksManager;
    private StatsManager statsManager;
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        tasksManager = new TasksManager(context);
        statsManager = new StatsManager(context);
        
        String  action         = intent.getAction( );
        int     notificationId = intent.getIntExtra("notificationID", -1);
        boolean isRecurring    = intent.getBooleanExtra("recurring", false);
        
        if (notificationId == -1)
            return;
        
        assert action != null;
        if (action.equals("Niewykonane"))
        {
            setTaskStatusNotDone(context, notificationId, isRecurring);
        }
        else if (action.equals("Wykonane"))
        {
            setTaskStatusDone(context, notificationId, isRecurring);
        }
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
    
    private void setTaskStatusDone(Context context, int id, boolean recurring)
    {
        SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           formattedDate = sdf.format(Calendar.getInstance( ).getTime( ));
        
        tasksManager.setTaskStatus(id, true);
        tasksManager.setLastDoneDate(id, formattedDate);
        
        statsManager.changeCount(formattedDate, recurring ? 2 : 1, 1, true);
        statsManager.changeCount(formattedDate, recurring ? 4 : 3, 1, false);
        
        Intent intent = new Intent("mat.szu.simplehabit.REFRESH_NEEDED");
        LocalBroadcastManager.getInstance(context.getApplicationContext( )).sendBroadcast(intent);
        
        Toast.makeText(context, "Wykonano", Toast.LENGTH_SHORT).show( );
    }
    
    private void setTaskStatusNotDone(Context context, int id, boolean recurring)
    {
        SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           formattedDate = sdf.format(Calendar.getInstance( ).getTime( ));
        Toast.makeText(context, "Nie wykonano", Toast.LENGTH_SHORT).show( );
        if (!tasksManager.getTaskById(id).isDone( ))
            return;
        tasksManager.setTaskStatus(id, false);
        tasksManager.undoLastDoneDate(id);
        statsManager.changeCount(formattedDate, recurring ? 2 : 1, 1, false);
        statsManager.changeCount(formattedDate, recurring ? 4 : 3, 1, true);
        
        Intent intent = new Intent("mat.szu.simplehabit.REFRESH_NEEDED");
        LocalBroadcastManager.getInstance(context.getApplicationContext( )).sendBroadcast(intent);
        
    }
}
