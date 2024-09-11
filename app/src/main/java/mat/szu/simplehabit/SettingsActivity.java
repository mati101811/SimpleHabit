package mat.szu.simplehabit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.opencsv.CSVWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SettingsActivity extends AppCompatActivity
{
    private static final String KEY_SORT_TASKS = "sortTasks";
    private static final String LAST_RUN_KEY = "lastRunDate";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String LAST_OPEN_DATE = "lastOpenDate";
    private static final String PERMISSIONS_SETTINGS = "permissionSettings";
    private static final String SHOW_TUTORIAL = "showTutorial";
    private static final int PICK_FOLDER_REQUEST_CODE = 1;
    private DatabaseHelper dbHelper;
    private int resetClicks = 0;
    
    private static void changeSortTaskOption(boolean isChecked, SharedPreferences sharedPreferences)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit( );
        editor.putBoolean(KEY_SORT_TASKS, isChecked);
        editor.apply( );
    }
    
    // Wywołaj ten kod, aby otworzyć wybór folderu
    public void pickFolder( )
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, PICK_FOLDER_REQUEST_CODE);
    }
    
    // Obsłuż wybór folderu i zapisz pliki CSV
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_FOLDER_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Uri folderUri = data.getData( );
            if (folderUri != null)
            {
                DocumentFile folderFile = DocumentFile.fromTreeUri(this, folderUri);
                
                if (folderFile != null && folderFile.isDirectory( ))
                {
                    // Zapisz dane z tabeli "tasks" do "SimpleHabitTasks.csv"
                    saveCsvFileToFolder(folderFile, "SimpleHabitTasks.csv", "SELECT * FROM tasks");
                    
                    // Zapisz dane z tabeli "stats" do "SimpleHabitStats.csv"
                    saveCsvFileToFolder(folderFile, "SimpleHabitStats.csv", "SELECT * FROM stats");
                    Toast.makeText(this, "Eksport udany", Toast.LENGTH_SHORT).show( );
                }
            }
        }
    }
    
    // Funkcja zapisująca pliki CSV do folderu
    private void saveCsvFileToFolder(DocumentFile folderFile, String fileName, String query)
    {
        try
        {
            // Utwórz nowy plik w wybranym folderze
            DocumentFile file = folderFile.createFile("text/csv", fileName);
            
            if (file != null)
            {
                Uri fileUri = file.getUri( );
                try (OutputStream outputStream = getContentResolver( ).openOutputStream(fileUri); CSVWriter csvWriter =
                        new CSVWriter(new OutputStreamWriter(outputStream)))
                {
                    
                    // Pobierz dane z bazy danych na podstawie zapytania SQL
                    Cursor cursor = dbHelper.getReadableDatabase( ).rawQuery(query, null);
                    csvWriter.writeNext(cursor.getColumnNames( ));  // Zapisz nagłówki kolumn
                    
                    // Zapisz dane z tabeli
                    while (cursor.moveToNext( ))
                    {
                        String[] rowData = new String[cursor.getColumnCount( )];
                        for (int i = 0; i < cursor.getColumnCount( ); i++)
                        {
                            rowData[i] = cursor.getString(i);
                        }
                        csvWriter.writeNext(rowData);
                    }
                    
                    // Zamknij zasoby
                    cursor.close( );
                }
            }
        } catch (Exception e)
        {
            Toast.makeText(this, "Błąd podczas eksportu", Toast.LENGTH_SHORT).show( );
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        dbHelper = new DatabaseHelper(this);
        
        MaterialButton reset = findViewById(R.id.reset);
        reset.setOnClickListener(v -> resetDatabase(sharedPreferences));
        
        Button back = findViewById(R.id.home);
        back.setOnClickListener(v -> finish( ));
        
        SwitchMaterial sortTasks = findViewById(R.id.sortTasks);
        sortTasks.setChecked(sharedPreferences.getBoolean(KEY_SORT_TASKS, true));
        sortTasks.setOnCheckedChangeListener((switchView, isChecked) -> changeSortTaskOption(isChecked, sharedPreferences));
        
        ImageButton bmcButton = findViewById(R.id.bmc_button);
        bmcButton.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/mati01811"))));
        
        Button permission = findViewById(R.id.permission);
        permission.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName( ));
            startActivity(intent);
        });
        
        Button export = findViewById(R.id.export);
        export.setOnClickListener(v -> pickFolder( ));
        
        Button tutorial = findViewById(R.id.tutorial);
        tutorial.setOnClickListener(v -> showTutorial(sharedPreferences));
    }
    
    private void showTutorial(SharedPreferences sharedPreferences)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit( );
        editor.putBoolean(SHOW_TUTORIAL, true);
        editor.apply( );
        
        Intent intent = getPackageManager( ).getLaunchIntentForPackage(getPackageName( ));
        if (intent != null)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish( );
        }
        
    }
    
    private void resetDatabase(SharedPreferences sharedPreferences)
    {
        if (resetClicks == 0)
        {
            Toast.makeText(this, "Naciśnij jeszcze raz aby potwierdzić", Toast.LENGTH_SHORT).show( );
            resetClicks++;
        }
        else
        {
            if (dbHelper.resetTables( ))
            {
                Toast.makeText(this, "Dane usunięte", Toast.LENGTH_SHORT).show( );
                SharedPreferences.Editor editor = sharedPreferences.edit( );
                editor.putLong(LAST_RUN_KEY, 0);
                editor.putString(LAST_OPEN_DATE, "");
                editor.apply( );
                recreate( );
            }
            else
                Toast.makeText(this, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show( );
            resetClicks = 0;
        }
    }
}