package mat.szu.simplehabit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsActivity extends AppCompatActivity
{
    private final int[] weekColors = new int[]{
            Color.rgb(222, 186, 160), Color.rgb(235, 214, 199), Color.rgb(222, 186, 160), Color.rgb(235, 214, 199), Color.rgb(222, 186, 160),
            Color.rgb(235, 214, 199)
    };
    private final String[] daysOfWeek = { "Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nie" };
    private StatsManager statsManager;
    
    private static int getFirstDayOfWeek( )
    {
        Calendar calendar = Calendar.getInstance( );
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Ustaw pierwszy dzień miesiąca
        
        // Uzyskaj pierwszy dzień tygodnia (poniedziałek jako pierwszy dzień tygodnia)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Jeśli pierwszy dzień tygodnia to niedziela, przestaw go na odpowiednie miejsce
        if (firstDayOfWeek == Calendar.SUNDAY)
        {
            firstDayOfWeek = 7; // Niedziela jako ostatni dzień tygodnia
        }
        else
        {
            firstDayOfWeek -= 1; // Przesunięcie dni tygodnia tak, aby poniedziałek był 1
        }
        return firstDayOfWeek;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        statsManager = new StatsManager(this);
        
        Button home = findViewById(R.id.home);
        home.setOnClickListener(v -> finish( ));
        
        Button settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
    
    @Override
    protected void onResume( )
    {
        super.onResume( );
        
        setTodayStats( );
        setAllStats( );
        setOnce( );
        setRepeat( );
        setWeeklyStats( );
        setWeeklyOnceStats( );
        setWeeklyRepeatStats( );
        setMonthlyStats( );
        setMonthlyOnceStats( );
        setMonthlyRepeatStats( );
    }
    
    private void setMonthlyStats( )
    {
        BarChart barChart = findViewById(R.id.monthChart);
        
        List<Integer>  monthlyData = statsManager.getMonthlyStats( );
        List<BarEntry> entries     = new ArrayList<>( );
        for (int i = 0; i < monthlyData.size( ); i++)
            entries.add(new BarEntry(i + 1, monthlyData.get(i)));
        
        int     firstDayOfWeek = getFirstDayOfWeek( );
        BarData barData        = getBarData(entries, firstDayOfWeek, monthlyData);
        barChart.setData(barData);
        
        XAxis xAxis = barChart.getXAxis( );
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        
        YAxis YAxisRight = barChart.getAxisRight( );
        YAxisRight.setAxisMaximum(100f);
        YAxis YAxisLeft = barChart.getAxisLeft( );
        YAxisLeft.setAxisMaximum(100f);
        
        barChart.getLegend( ).setEnabled(false);
        barChart.getDescription( ).setEnabled(false);
        barChart.invalidate( );
    }
    
    private void setMonthlyOnceStats( )
    {
        BarChart barChart = findViewById(R.id.monthChartOnce);
        
        List<Integer>  monthlyData = statsManager.getMonthlyOnceStats( );
        List<BarEntry> entries     = new ArrayList<>( );
        for (int i = 0; i < monthlyData.size( ); i++)
            entries.add(new BarEntry(i + 1, monthlyData.get(i)));
        
        int     firstDayOfWeek = getFirstDayOfWeek( );
        BarData barData        = getBarData(entries, firstDayOfWeek, monthlyData);
        barChart.setData(barData);
        
        XAxis xAxis = barChart.getXAxis( );
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        
        YAxis YAxisRight = barChart.getAxisRight( );
        YAxisRight.setAxisMaximum(100f);
        YAxis YAxisLeft = barChart.getAxisLeft( );
        YAxisLeft.setAxisMaximum(100f);
        
        barChart.getLegend( ).setEnabled(false);
        barChart.getDescription( ).setEnabled(false);
        barChart.invalidate( );
    }
    
    private @NonNull BarData getBarData(List<BarEntry> entries, int firstDayOfWeek, List<Integer> monthlyData)
    {
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Data");
        
        List<Integer> colors         = new ArrayList<>( );
        int           currentWeekDay = firstDayOfWeek;
        int           weekColorIndex = 0;
        
        for (int i = 0; i < monthlyData.size( ); i++)
        {
            if (currentWeekDay > 7)
            {
                currentWeekDay = 1;
                weekColorIndex++;
            }
            colors.add(weekColors[weekColorIndex % weekColors.length]);
            currentWeekDay++;
        }
        
        dataSet.setColors(colors);
        
        return new BarData(dataSet);
    }
    
    private void setMonthlyRepeatStats( )
    {
        BarChart barChart = findViewById(R.id.monthChartRepeat);
        
        List<Integer>  monthlyData = statsManager.getMonthlyRepeatStats( );
        List<BarEntry> entries     = new ArrayList<>( );
        for (int i = 0; i < monthlyData.size( ); i++)
            entries.add(new BarEntry(i + 1, monthlyData.get(i)));
        
        int     firstDayOfWeek = getFirstDayOfWeek( );
        BarData barData        = getBarData(entries, firstDayOfWeek, monthlyData);
        barChart.setData(barData);
        
        XAxis xAxis = barChart.getXAxis( );
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        
        YAxis YAxisRight = barChart.getAxisRight( );
        YAxisRight.setAxisMaximum(100f);
        YAxis YAxisLeft = barChart.getAxisLeft( );
        YAxisLeft.setAxisMaximum(100f);
        
        barChart.getLegend( ).setEnabled(false);
        barChart.getDescription( ).setEnabled(false);
        barChart.invalidate( );
    }
    
    private void setWeeklyStats( )
    {
        CombinedChart combinedChart = findViewById(R.id.weekChart);
        
        String   data    = statsManager.getWeeklyStats( );
        String[] dayData = data.split("\\),\\(");
        
        List<BarEntry> totalTasksEntries     = new ArrayList<>( );
        List<Entry>    completedTasksEntries = new ArrayList<>( );
        
        for (int i = 0; i < dayData.length; i++)
        {
            String   cleanData      = dayData[i].replace("(", "").replace(")", "");
            String[] values         = cleanData.split(", ");
            int      totalTasks     = Integer.parseInt(values[0]);
            int      completedTasks = Integer.parseInt(values[1]);
            
            totalTasksEntries.add(new BarEntry(i, totalTasks));
            completedTasksEntries.add(new Entry(i, completedTasks));
        }
        
        BarDataSet totalTasksDataSet = new BarDataSet(totalTasksEntries, "Wszystkie zadania");
        totalTasksDataSet.setValueTextColor(Color.BLACK);
        int barColor = Color.rgb(221, 186, 161);
        totalTasksDataSet.setColor(barColor);
        totalTasksDataSet.setValueTextSize(12f);
        
        LineDataSet completedTasksDataSet = new LineDataSet(completedTasksEntries, "Wykonane zadania");
        completedTasksDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.primary));
        int lineColor = ContextCompat.getColor(this, R.color.primary);
        completedTasksDataSet.setColor(lineColor);
        completedTasksDataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setCircleHoleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setValueTextSize(12f);
        
        CustomValueFormatter customFormatter = new CustomValueFormatter(completedTasksDataSet.getValues( ));
        totalTasksDataSet.setValueFormatter(customFormatter);
        completedTasksDataSet.setValueFormatter(customFormatter);
        
        BarData  barData  = new BarData(totalTasksDataSet);
        LineData lineData = new LineData(completedTasksDataSet);
        
        CombinedData combinedData = new CombinedData( );
        combinedData.setData(barData);
        combinedData.setData(lineData);
        
        XAxis xAxis = combinedChart.getXAxis( );
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        barData.setBarWidth(0.4f);
        
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(dayData.length - 0.5f);
        
        YAxis yAxisLeft = combinedChart.getAxisLeft( );
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMinimum(0f);
        
        YAxis yAxisRight = combinedChart.getAxisRight( );
        yAxisRight.setGranularity(1f);
        yAxisRight.setAxisMinimum(0f);
        
        combinedChart.setData(combinedData);
        combinedChart.getDescription( ).setEnabled(false);
        combinedChart.invalidate( );
    }
    
    private void setWeeklyOnceStats( )
    {
        CombinedChart combinedChart = findViewById(R.id.weekChartOnce);
        
        String         data                  = statsManager.getWeeklyOnceStats( );
        String[]       dayData               = data.split("\\),\\(");
        List<BarEntry> totalTasksEntries     = new ArrayList<>( );
        List<Entry>    completedTasksEntries = new ArrayList<>( );
        
        for (int i = 0; i < dayData.length; i++)
        {
            String   cleanData      = dayData[i].replace("(", "").replace(")", "");
            String[] values         = cleanData.split(", ");
            int      totalTasks     = Integer.parseInt(values[0]);
            int      completedTasks = Integer.parseInt(values[1]);
            
            totalTasksEntries.add(new BarEntry(i, totalTasks));
            completedTasksEntries.add(new Entry(i, completedTasks));
        }
        
        BarDataSet totalTasksDataSet = new BarDataSet(totalTasksEntries, "Wszystkie zadania");
        totalTasksDataSet.setValueTextColor(Color.BLACK);
        int barColor = Color.rgb(221, 186, 161);
        totalTasksDataSet.setColor(barColor);
        totalTasksDataSet.setValueTextSize(12f);
        
        LineDataSet completedTasksDataSet = new LineDataSet(completedTasksEntries, "Wykonane zadania");
        completedTasksDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.primary));
        int lineColor = ContextCompat.getColor(this, R.color.primary);
        completedTasksDataSet.setColor(lineColor);
        completedTasksDataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setCircleHoleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setValueTextSize(12f);
        
        CustomValueFormatter customFormatter = new CustomValueFormatter(completedTasksDataSet.getValues( ));
        totalTasksDataSet.setValueFormatter(customFormatter);
        completedTasksDataSet.setValueFormatter(customFormatter);
        
        BarData  barData  = new BarData(totalTasksDataSet);
        LineData lineData = new LineData(completedTasksDataSet);
        
        CombinedData combinedData = new CombinedData( );
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
        
        XAxis xAxis = combinedChart.getXAxis( );
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(dayData.length - 0.5f);
        
        barData.setBarWidth(0.4f);
        
        YAxis yAxisLeft = combinedChart.getAxisLeft( );
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMinimum(0f);
        
        YAxis yAxisRight = combinedChart.getAxisRight( );
        yAxisRight.setGranularity(1f);
        yAxisRight.setAxisMinimum(0f);
        
        combinedChart.getDescription( ).setEnabled(false);
        combinedChart.invalidate( );
    }
    
    private void setWeeklyRepeatStats( )
    {
        CombinedChart combinedChart = findViewById(R.id.weekChartRepeat);
        
        String         data                  = statsManager.getWeeklyRepeatStats( );
        String[]       dayData               = data.split("\\),\\(");
        List<BarEntry> totalTasksEntries     = new ArrayList<>( );
        List<Entry>    completedTasksEntries = new ArrayList<>( );
        
        for (int i = 0; i < dayData.length; i++)
        {
            String   cleanData      = dayData[i].replace("(", "").replace(")", "");
            String[] values         = cleanData.split(", ");
            int      totalTasks     = Integer.parseInt(values[0]);
            int      completedTasks = Integer.parseInt(values[1]);
            
            totalTasksEntries.add(new BarEntry(i, totalTasks));
            completedTasksEntries.add(new Entry(i, completedTasks));
        }
        
        BarDataSet totalTasksDataSet = new BarDataSet(totalTasksEntries, "Wszystkie zadania");
        totalTasksDataSet.setValueTextColor(Color.BLACK);
        int barColor = Color.rgb(221, 186, 161);
        totalTasksDataSet.setColor(barColor);
        totalTasksDataSet.setValueTextSize(12f);
        
        LineDataSet completedTasksDataSet = new LineDataSet(completedTasksEntries, "Wykonane zadania");
        completedTasksDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.primary));
        int lineColor = ContextCompat.getColor(this, R.color.primary);
        completedTasksDataSet.setColor(lineColor);
        completedTasksDataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setCircleHoleColor(ContextCompat.getColor(this, R.color.primary));
        completedTasksDataSet.setValueTextSize(12f);
        
        CustomValueFormatter customFormatter = new CustomValueFormatter(completedTasksDataSet.getValues( ));
        totalTasksDataSet.setValueFormatter(customFormatter);
        completedTasksDataSet.setValueFormatter(customFormatter);
        
        BarData  barData  = new BarData(totalTasksDataSet);
        LineData lineData = new LineData(completedTasksDataSet);
        
        CombinedData combinedData = new CombinedData( );
        combinedData.setData(barData);
        combinedData.setData(lineData);
        
        combinedChart.setData(combinedData);
        
        XAxis xAxis = combinedChart.getXAxis( );
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        barData.setBarWidth(0.4f);
        
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(dayData.length - 0.5f);
        
        YAxis yAxisLeft = combinedChart.getAxisLeft( );
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMinimum(0f);
        
        YAxis yAxisRight = combinedChart.getAxisRight( );
        yAxisRight.setGranularity(1f);
        yAxisRight.setAxisMinimum(0f);
        
        combinedChart.getDescription( ).setEnabled(false);
        combinedChart.invalidate( );
    }
    
    private void setRepeat( )
    {
        TextView repeatDone            = findViewById(R.id.repeat_done);
        TextView repeatNotDone         = findViewById(R.id.repeat_not_done);
        TextView repeatAll             = findViewById(R.id.repeat_all);
        TextView repeatSuccess         = findViewById(R.id.repeat_success);
        int      allRepeatDoneCount    = statsManager.countAllRepeatDone( );
        int      allRepeatNotDoneCount = statsManager.countAllRepeatNotDone( );
        int      allRepeatCount        = allRepeatDoneCount + allRepeatNotDoneCount;
        int      allRepeatSuccessCount = allRepeatCount == 0 ? 0 : allRepeatDoneCount * 100 / allRepeatCount;
        repeatAll.setText(String.valueOf(allRepeatCount));
        repeatDone.setText(String.valueOf(allRepeatDoneCount));
        repeatNotDone.setText(String.valueOf(allRepeatNotDoneCount));
        String allRepeatSuccess = allRepeatSuccessCount + "%";
        repeatSuccess.setText(allRepeatSuccess);
    }
    
    private void setOnce( )
    {
        TextView onceDone            = findViewById(R.id.once_done);
        TextView onceNotDone         = findViewById(R.id.once_not_done);
        TextView onceAll             = findViewById(R.id.once_all);
        TextView onceSuccess         = findViewById(R.id.once_success);
        int      allOnceDoneCount    = statsManager.countAllOnceDone( );
        int      allOnceNotDoneCount = statsManager.countAllOnceNotDone( );
        int      allOnceCount        = allOnceDoneCount + allOnceNotDoneCount;
        int      allOnceSuccessCount = allOnceCount == 0 ? 0 : allOnceDoneCount * 100 / allOnceCount;
        onceAll.setText(String.valueOf(allOnceCount));
        onceDone.setText(String.valueOf(allOnceDoneCount));
        onceNotDone.setText(String.valueOf(allOnceNotDoneCount));
        String allOnceSuccess = allOnceSuccessCount + "%";
        onceSuccess.setText(allOnceSuccess);
    }
    
    private void setAllStats( )
    {
        TextView all             = findViewById(R.id.all);
        TextView allDone         = findViewById(R.id.done);
        TextView allNotDone      = findViewById(R.id.not_done);
        TextView allSuccess      = findViewById(R.id.success);
        int      allDoneCount    = statsManager.countAllDone( );
        int      allNotDoneCount = statsManager.countAllNotDone( );
        int      allCount        = allDoneCount + allNotDoneCount;
        int      allSuccessCount = allCount == 0 ? 0 : allDoneCount * 100 / allCount;
        all.setText(String.valueOf(allCount));
        allDone.setText(String.valueOf(allDoneCount));
        allNotDone.setText(String.valueOf(allNotDoneCount));
        String allSuccessString = allSuccessCount + "%";
        allSuccess.setText(allSuccessString);
    }
    
    private void setTodayStats( )
    {
        TextView doneToday         = findViewById(R.id.done_today);
        TextView notDoneToday      = findViewById(R.id.not_done_today);
        int      doneTodayCount    = statsManager.countDoneToday( );
        int      notDoneTodayCount = statsManager.countNotDoneToday( );
        doneToday.setText(String.valueOf(doneTodayCount));
        notDoneToday.setText(String.valueOf(notDoneTodayCount));
    }
}