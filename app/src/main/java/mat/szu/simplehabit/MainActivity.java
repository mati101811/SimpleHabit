package mat.szu.simplehabit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    private static final String PREFS_NAME = "MyPrefs";
    private static final String CHANNEL_ID = "simple_habit_notification_channel";
    private static final String LAST_RUN_KEY = "lastRunDate";
    private static final String KEY_SORT_TASKS = "sortTasks";
    private static final String LAST_OPEN_DATE = "lastOpenDate";
    private static final String PERMISSIONS_SETTINGS = "permissionSettings";
    private static final String SHOW_TUTORIAL = "showTutorial";
    private static final String TAG = "debugowanie";
    private final BroadcastReceiver dataChangeReceiver = new BroadcastReceiver( )
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            recreate( );
        }
    };
    int tutorialPage = 1;
    MaterialButton add;
    LinearLayout toDo;
    LinearLayout toDoDone;
    LinearLayout everydayDone;
    LinearLayout everyday;
    Button statsButton;
    Button settings;
    SharedPreferences sharedPreferences;
    private TasksManager tasksManager;
    private StatsManager statsManager;
    private boolean sort;
    
    private static long getDelayInMillis(int hour, int minutes)
    {
        Calendar currentTime = Calendar.getInstance( );
        
        Calendar targetTime = Calendar.getInstance( );
        targetTime.set(Calendar.HOUR_OF_DAY, hour);
        targetTime.set(Calendar.MINUTE, minutes);
        targetTime.set(Calendar.SECOND, 0);
        
        // Sprawdź, czy targetTime jest w przeszłości w stosunku do currentTime
        if (targetTime.before(currentTime))
        {
            targetTime.add(Calendar.DAY_OF_MONTH, 1); // Dodaj jeden dzień
        }
        
        return targetTime.getTimeInMillis( ) - currentTime.getTimeInMillis( );
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        if (sharedPreferences.getBoolean(SHOW_TUTORIAL, true))
        {
            openTutorialDialog( );
            SharedPreferences.Editor editor = sharedPreferences.edit( );
            editor.putBoolean(SHOW_TUTORIAL, false);
            editor.apply( );
        }
        if (!sharedPreferences.getBoolean(PERMISSIONS_SETTINGS, false))
        {
            createNotificationChannel( );
            if (!areNotificationsEnabled( ))
                openPermissionDialog( );
            SharedPreferences.Editor editor = sharedPreferences.edit( );
            editor.putBoolean(PERMISSIONS_SETTINGS, true);
            editor.apply( );
        }
        
        toDo         = findViewById(R.id.to_do);
        everyday     = findViewById(R.id.everyday);
        toDoDone     = findViewById(R.id.to_do_done);
        everydayDone = findViewById(R.id.everyday_done);
        
        add = findViewById(R.id.add);
        add.setOnClickListener(v -> openAddingDialog( ));
        
        statsButton = findViewById(R.id.stats);
        statsButton.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));
        
        settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        
        tasksManager = new TasksManager(this);
        statsManager = new StatsManager(this);
    }
    
    @Override
    protected void onPause( )
    {
        super.onPause( );
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangeReceiver);
    }
    
    @Override
    protected void onResume( )
    {
        super.onResume( );
        sort = sharedPreferences.getBoolean(KEY_SORT_TASKS, true);
        IntentFilter filter = new IntentFilter("mat.szu.simplehabit.REFRESH_NEEDED");
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangeReceiver, filter);
        
        everyday.removeAllViews( );
        everydayDone.removeAllViews( );
        toDo.removeAllViews( );
        toDoDone.removeAllViews( );
        
        List<Task> taskList = tasksManager.getAllActiveTasks( );
        for (Task task : taskList)
            showTask((int) task.getId( ), task.getTaskName( ), task.getNotificationTime( ), task.isRecurring( ), task.isDone( ));
        
        checkDate( );
    }
    
    private void checkDate( )
    {
        long lastRunMillis = sharedPreferences.getLong(LAST_RUN_KEY, 0);
        
        Calendar todayCalendar = Calendar.getInstance( );
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        
        if (lastRunMillis != 0)
        {
            Calendar lastRunCalendar = Calendar.getInstance( );
            lastRunCalendar.setTimeInMillis(lastRunMillis);
            lastRunCalendar.set(Calendar.HOUR_OF_DAY, 0);
            lastRunCalendar.set(Calendar.MINUTE, 0);
            lastRunCalendar.set(Calendar.SECOND, 0);
            lastRunCalendar.set(Calendar.MILLISECOND, 0);
            
            if (!lastRunCalendar.equals(todayCalendar))
            {
                if (lastRunCalendar.before(todayCalendar))
                {
                    openFinishDialog(lastRunCalendar);
                    tasksManager.resetActiveTaskStatus( );
                    SimpleDateFormat sdf               = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
                    String           formattedDate     = sdf.format(todayCalendar.getTime( ));
                    int              activeRepeatCount = tasksManager.getActiveRepeatTasksCount( );
                    statsManager.changeCount(formattedDate, 4, activeRepeatCount, true);
                    tasksManager.deactivateNonRecurringTasks( );
                    
                    SharedPreferences.Editor editor = sharedPreferences.edit( );
                    editor.putLong(LAST_RUN_KEY, System.currentTimeMillis( ));
                    editor.putString(LAST_OPEN_DATE, formattedDate);
                    editor.apply( );
                }
                else
                {
                    everyday.removeAllViews( );
                    everydayDone.removeAllViews( );
                    toDo.removeAllViews( );
                    toDoDone.removeAllViews( );
                    add.setVisibility(View.GONE);
                    openTimeTravelDialog( );
                    
                }
            }
        }
        else
        {
            if (add.getVisibility( ) == View.GONE)
                add.setVisibility(View.VISIBLE);
            
            SimpleDateFormat         sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
            String                   formattedDate = sdf.format(todayCalendar.getTime( ));
            SharedPreferences.Editor editor        = sharedPreferences.edit( );
            editor.putLong(LAST_RUN_KEY, System.currentTimeMillis( ));
            editor.putString(LAST_OPEN_DATE, formattedDate);
            editor.apply( );
        }
    }
    
    private void openTimeTravelDialog( )
    {
        AlertDialog.Builder builder      = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater      inflater     = getLayoutInflater( );
        View                dialogLayout = inflater.inflate(R.layout.time_travel_dialog, null);
        Button              ok           = dialogLayout.findViewById(R.id.ok);
        builder.setView(dialogLayout);
        AlertDialog dialog = builder.create( );
        ok.setOnClickListener(v -> dialog.dismiss( ));
        dialog.show( );
    }
    
    private void openFinishDialog(Calendar previousDay)
    {
        AlertDialog.Builder builder      = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater      inflater     = getLayoutInflater( );
        View                dialogLayout = inflater.inflate(R.layout.finish_tasks_dialog, null);
        LinearLayout        tasksLayout  = dialogLayout.findViewById(R.id.tasks_to_finish);
        SimpleDateFormat    sdf          = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        
        String formattedDate = sdf.format(previousDay.getTime( ));
        
        List<Task> taskList = tasksManager.getUnfinishedTasks(formattedDate);
        
        for (Task task : taskList)
        {
            LayoutInflater itemInflater = LayoutInflater.from(this);
            View           item         = itemInflater.inflate(R.layout.item, tasksLayout, false);
            TextView       taskString   = item.findViewById(R.id.task_name);
            TextView       notification = item.findViewById(R.id.notification_time);
            CheckBox       done         = item.findViewById(R.id.done);
            
            done.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int columnNumberToAdd      = (!task.isRecurring( ) ? 1 : 2) + (isChecked ? 0 : 2);
                int columnNumberToSubtract = (!task.isRecurring( ) ? 1 : 2) + (isChecked ? 2 : 0);
                statsManager.changeCount(formattedDate, columnNumberToAdd, 1, true);
                statsManager.changeCount(formattedDate, columnNumberToSubtract, 1, false);
                if (isChecked)
                    tasksManager.setLastDoneDate((int) task.getId( ), formattedDate);
                else
                    tasksManager.undoLastDoneDate((int) task.getId( ));
            });
            
            taskString.setText(task.getTaskName( ));
            notification.setText(task.getNotificationTime( ));
            tasksLayout.addView(item);
        }
        builder.setView(dialogLayout);
        AlertDialog dialog = builder.create( );
        Button      finish = dialogLayout.findViewById(R.id.finish);
        finish.setOnClickListener(v -> {
            dialog.dismiss( );
            recreate( );
        });
        if (!taskList.isEmpty( ))
            dialog.show( );
    }
    
    private boolean areNotificationsEnabled( )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            return notificationManager.areNotificationsEnabled( );
        }
        return true;
    }
    
    private void openPermissionDialog( )
    {
        AlertDialog.Builder builder      = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater      inflater     = getLayoutInflater( );
        View                dialogLayout = inflater.inflate(R.layout.permission_dialog, null);
        TextView            deny         = dialogLayout.findViewById(R.id.odmow);
        Button              openSetting  = dialogLayout.findViewById(R.id.allow);
        String              myText       = "<u>Odmów</u>";
        deny.setText(Html.fromHtml(myText));
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create( );
        openSetting.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName( ));
                startActivity(intent);
                dialog.dismiss( );
            }
        });
        deny.setOnClickListener(v -> dialog.dismiss( ));
        
        dialog.show( );
    }
    
    private void openTutorialDialog( )
    {
        AlertDialog.Builder builder      = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater      inflater     = getLayoutInflater( );
        View                dialogLayout = inflater.inflate(R.layout.tutorial_dialog, null);
        Button              next         = dialogLayout.findViewById(R.id.next);
        TextView            text         = dialogLayout.findViewById(R.id.tutorial_text);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create( );
        text.setText("Aplikacja pozwala na tworzenie dwóch typów zadań:\n1. Do zrobienia\n2. Codzienne");
        next.setOnClickListener(v -> {
            tutorialPage++;
            if (tutorialPage == 2)
                text.setText("Zadania do zrobienia (jednokrotne) kończą się wraz z końcem dnia i znikają z listy albo wykonane albo nie.");
            if (tutorialPage == 3)
                text.setText("Zadania codzienne (wielokrotne) zostają na liście do momentu ich ręcznego zakończenia. Po każdym dniu ich aktualny "
                             + "stan zapisuje się w statystykach, a następnie ustawiane są jako niewykonane. Pozwala to na codzienne ich "
                             + "wykonywanie o określonej porze bez konieczności codziennego dodawania.");
            if (tutorialPage == 4)
                text.setText("Aby zakończyć wyświetlanie codziennego zadania należy przytrzymać je wciśnięte do momentu zniknięcia.");
            if (tutorialPage == 5)
                text.setText("Przed zakończeniem codziennego zadania warto je wykonać, ponieważ po usunięciu dalej będzie liczyć się w "
                             + "statystykach, a nie będzie można zmienić jego stanu.");
            if (tutorialPage == 6)
                text.setText("Podczas dodawania nowego zadania nie można wybrać godziny między 23:55 a 00:05, aby nie narażać się na tworzenie błędów"
                             + " związanych ze zmianą dnia.");
            if (tutorialPage == 7)
            {
                text.setText("Podsumowanie tygodnia w statystykach podane jest w liczbie zadań, a podsumowanie miesiąca to procent wykonania zadań "
                             + "na dany dzień.");
            }
            if (tutorialPage == 9)
            {
                text.setText("W statystykach termin \"zadanie\" odnosi się do pojedynczego zadania jednokrotnego, albo do każdego dnia w którym "
                             + "zadanie było aktywne w przypadku zadań wielokrotnych.");
                next.setText("Zakończ");
            }
            if (tutorialPage == 8)
                dialog.dismiss( );
        });
        
        dialog.show( );
    }
    
    private void openAddingDialog( )
    {
        AlertDialog.Builder builder      = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater      inflater     = getLayoutInflater( );
        View                dialogLayout = inflater.inflate(R.layout.adding_dialog, null);
        builder.setView(dialogLayout);
        
        AlertDialog    dialog = builder.create( );
        MaterialButton close  = dialogLayout.findViewById(R.id.anuluj);
        close.setOnClickListener(v -> dialog.dismiss( ));
        
        TextView   taskName   = dialogLayout.findViewById(R.id.tresc_zadania);
        TimePicker timePicker = dialogLayout.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        
        MaterialButton add = dialogLayout.findViewById(R.id.dodaj);
        add.setOnClickListener(v -> {
            if (taskName.getText( ).toString( ).isEmpty( ))
            {
                taskName.setError("To pole nie może być puste");
                return;
            }
            int hour    = timePicker.getHour( );
            int minutes = timePicker.getMinute( );
            
            if ((hour == 23 && minutes >= 55) || (hour == 0 && minutes <= 5))
            {
                Toast.makeText(this, "Zabezpieczenie: Nie można ustawiać czasu koło północy", Toast.LENGTH_SHORT).show( );
                return;
            }
            
            MaterialButtonToggleGroup repeat           = dialogLayout.findViewById(R.id.powtarzanie);
            int                       selectedButtonId = repeat.getCheckedButtonId( );
            
            boolean recurring        = selectedButtonId == R.id.powtorz_codziennie;
            String  notificationTime = (hour < 10 ? "0" + hour : hour) + ":" + (minutes < 10 ? "0" + minutes : minutes);
            int     id               = addTask(String.valueOf(taskName.getText( )), notificationTime, recurring);
            showTask(id, String.valueOf(taskName.getText( )), notificationTime, recurring, false);
            scheduleNotification(id, String.valueOf(taskName.getText( )), hour, minutes, recurring);
            
            dialog.dismiss( );
        });
        
        dialog.show( );
    }
    
    private int addTask(String taskName, String notificationTime, boolean recurring)
    {
        Calendar         calendar      = Calendar.getInstance( );
        SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           formattedDate = sdf.format(calendar.getTime( ));
        statsManager.changeCount(formattedDate, recurring ? 4 : 3, 1, true);
        return tasksManager.addTask(taskName, formattedDate, formattedDate, notificationTime, recurring, true, false);
    }
    
    private void showTask(int id, String taskName, String notificationTime, boolean recurring, boolean isDone)
    {
        LayoutInflater inflater     = LayoutInflater.from(this);
        View           item         = inflater.inflate(R.layout.item, toDo, false);
        TextView       taskString   = item.findViewById(R.id.task_name);
        TextView       notification = item.findViewById(R.id.notification_time);
        CheckBox       done         = item.findViewById(R.id.done);
        if (isDone)
            done.setChecked(true);
        
        done.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Calendar         calendar      = Calendar.getInstance( );
            SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
            String           formattedDate = sdf.format(calendar.getTime( ));
            tasksManager.setTaskStatus(id, buttonView.isChecked( ));
            int columnNumberToAdd      = (!recurring ? 1 : 2) + (isChecked ? 0 : 2);
            int columnNumberToSubtract = (!recurring ? 1 : 2) + (isChecked ? 2 : 0);
            statsManager.changeCount(formattedDate, columnNumberToAdd, 1, true);
            statsManager.changeCount(formattedDate, columnNumberToSubtract, 1, false);
            if (sort)
                moveTask(item, recurring, buttonView.isChecked( ));
            if (isChecked)
                tasksManager.setLastDoneDate(id, formattedDate);
            else
                tasksManager.undoLastDoneDate(id);
        });
        
        if (recurring)
            item.setOnLongClickListener(v -> closeTask(v, id));
        
        taskString.setText(taskName);
        notification.setText(notificationTime);
        if (sort)
        {
            if (recurring)
                if (isDone)
                    everydayDone.addView(item);
                else
                    everyday.addView(item);
            else if (isDone)
                toDoDone.addView(item);
            else
                toDo.addView(item);
        }
        else
        {
            if (recurring)
                everydayDone.addView(item);
            else
                toDo.addView(item);
        }
    }
    
    private boolean closeTask(View view, int id)
    {
        Calendar         calendar      = Calendar.getInstance( );
        SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault( ));
        String           formattedDate = sdf.format(calendar.getTime( ));
        
        tasksManager.deactivateTask(id);
        tasksManager.updateTaskEndDate(id, formattedDate);
        
        ViewGroup.LayoutParams layoutParams  = view.getLayoutParams( );
        int                    initialHeight = view.getHeight( );
        
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(300);
        
        animator.addUpdateListener(valueAnimator -> {
            layoutParams.height = (int) valueAnimator.getAnimatedValue( );
            view.setLayoutParams(layoutParams);
        });
        
        animator.addListener(new AnimatorListenerAdapter( )
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                ViewGroup parent = (ViewGroup) view.getParent( );
                if (parent != null)
                {
                    parent.removeView(view);
                }
            }
        });
        
        animator.start( );
        return true;
    }
    
    private void moveTask(View view, boolean recurring, boolean isDone)
    {
        LinearLayout parent = (LinearLayout) view.getParent( );
        parent.removeView(view);
        if (recurring)
            if (isDone)
                everydayDone.addView(view);
            else
                everyday.addView(view);
        else if (isDone)
            toDoDone.addView(view);
        else
            toDo.addView(view);
    }
    
    private void scheduleNotification(int taskId, String content, int hour, int minutes, boolean recurring)
    {
        long delayInMillis = getDelayInMillis(hour, minutes);
        Data data =
                new Data.Builder( ).putString("title", "Zadanie zrobione?").putString("content", content).putInt("id", taskId).putBoolean(
                        "recurring", recurring).putInt("hour", hour).putInt("minutes", minutes).build( );
        
        WorkRequest notificationWork =
                new OneTimeWorkRequest.Builder(NotificationWorker.class).setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS).setInputData(data).build( );
        
        WorkManager.getInstance(this).enqueue(notificationWork);
    }
    
    void createNotificationChannel( )
    {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        NotificationChannel existingChannel     = notificationManager.getNotificationChannel(CHANNEL_ID);
        if (existingChannel == null)
        {
            CharSequence        name       = "Task Notifications";
            int                 importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel    = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }
}