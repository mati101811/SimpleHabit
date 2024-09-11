package mat.szu.simplehabit;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class CustomValueFormatter extends ValueFormatter
{
    private final List<Entry> lineEntries;
    
    public CustomValueFormatter(List<Entry> lineEntries)
    {
        this.lineEntries = lineEntries;
    }
    
    @Override
    public String getFormattedValue(float value)
    {
        for (Entry lineEntry : lineEntries)
            if (lineEntry.getX( ) == value)
                return value % 1 == 0 ? String.format("%d", (int) value) : String.format("%.1f", value);
        return super.getFormattedValue(value);
    }
    
    @Override
    public String getBarLabel(BarEntry barEntry)
    {
        for (Entry lineEntry : lineEntries)
            if (lineEntry.getX( ) == barEntry.getX( ))
                if (lineEntry.getY( ) == barEntry.getY( ))
                    return "";
        
        float value = barEntry.getY( );
        return value % 1 == 0 ? String.format("%d", (int) value) : String.format("%.1f", value);
    }
}
