package mat.szu.simplehabit;

import androidx.annotation.NonNull;

public class Task
{
    
    // Pola odpowiadające kolumnom tabeli
    private long id;
    private String taskName;
    private String startDate;
    private String endDate;
    private String notificationTime;
    private boolean isRecurring;
    private boolean isActive;
    private boolean isDone;
    private String lastLastDone;
    private String lastDone;
    
    // Konstruktor
    public Task(long id, String taskName, String startDate, String endDate, String notificationTime, boolean isRecurring, boolean isActive,
                boolean isDone, String lastLastDone, String lastDone)
    {
        this.id               = id;
        this.taskName         = taskName;
        this.startDate        = startDate;
        this.endDate          = endDate;
        this.notificationTime = notificationTime;
        this.isRecurring      = isRecurring;
        this.isActive         = isActive;
        this.isDone           = isDone;
        this.lastLastDone     = lastLastDone;
        this.lastDone         = lastDone;
    }
    
    // Gettery i settery
    public long getId( )
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    public String getTaskName( )
    {
        return taskName;
    }
    
    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }
    
    public String getStartDate( )
    {
        return startDate;
    }
    
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }
    
    public String getEndDate( )
    {
        return endDate;
    }
    
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }
    
    public String getNotificationTime( )
    {
        return notificationTime;
    }
    
    public void setNotificationTime(String notificationTime)
    {
        this.notificationTime = notificationTime;
    }
    
    public boolean isRecurring( )
    {
        return isRecurring;
    }
    
    public void setRecurring(boolean recurring)
    {
        isRecurring = recurring;
    }
    
    public boolean isActive( )
    {
        return isActive;
    }
    
    public void setActive(boolean active)
    {
        isActive = active;
    }
    
    public boolean isDone( )
    {
        return isDone;
    }
    
    public void setDone(boolean done)
    {
        isDone = done;
    }
    
    @NonNull
    @Override
    public String toString( )
    {
        return "Task{" + "id=" + id + ", taskName='" + taskName + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", "
                + "notificationTime='" + notificationTime + '\'' + ", isRecurring=" + isRecurring + ", isActive=" + isActive + ", isDone=" + isDone
                + '}';
    }
    
    public String getLastDone( )
    {
        return lastDone;
    }
    
    public void setLastDone(String lastDone)
    {
        this.lastDone = lastDone;
    }
    
    public String getLastLastDone( )
    {
        return lastLastDone;
    }
    
    public void setLastLastDone(String lastLastDone)
    {
        this.lastLastDone = lastLastDone;
    }
}
