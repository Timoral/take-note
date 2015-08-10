package com.timmo.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_CHECKLISTS = "checklists";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (sharedPreferences.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);


        Preference preferenceTheme = findPreference("dark_theme");
        preferenceTheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                finish();
                return false;
            }
        });
        Preference preferenceExport = findPreference("notes_export");
        preferenceExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ExportTableToCSV(TABLE_NOTES);
                return false;
            }
        });
        Preference preferenceReset = findPreference("reset");
        preferenceReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sharedPreferences.edit().clear().apply();
                finish();
                startActivity(getIntent());
                return false;
            }
        });
        Preference preferenceClear = findPreference("clear");
        preferenceClear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NotesDatabaseHandler db = new NotesDatabaseHandler(SettingsActivity.this);
                db.RecreateTable();
                startActivity(new Intent(SettingsActivity.this, NotesActivity.class));
                finish();
                return false;
            }
        });
        Preference preferenceClearAll = findPreference("clear_all");
        preferenceClearAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sharedPreferences.edit().clear().apply();
                NotesDatabaseHandler db = new NotesDatabaseHandler(SettingsActivity.this);
                db.RecreateTable();
                startActivity(new Intent(SettingsActivity.this, NotesActivity.class));
                finish();
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //setupSimplePreferencesScreen();
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        toolbar.setTitle(getString(R.string.title_activity_settings));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        //toolbar.inflateMenu(R.menu.menu_main);
        //toolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, NotesActivity.class));
        super.onBackPressed();
    }

    private void ExportTableToCSV(String table) {
        NotesDatabaseHandler notesDatabaseHandler = new NotesDatabaseHandler(SettingsActivity.this);

        //File dbFile = getDatabasePath("YourDatabase.db");

        File exportDir = new File(Environment.getExternalStorageDirectory().getPath(), "");

/*
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
*/

        String filename = "TakeNoteExported" + table + ".csv";

        File file = new File(exportDir, filename);

        try {
            //file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            SQLiteDatabase db = notesDatabaseHandler.getReadableDatabase();

            Cursor curCSV = db.rawQuery("SELECT * FROM " + table, null);
            //  int c = curCSV.getColumnCount();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1),
                        curCSV.getString(2), curCSV.getString(3)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            Toast.makeText(this, filename + " saved to sdcard/", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }


}