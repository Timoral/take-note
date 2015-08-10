package com.timmo.notes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

class NotesDatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "TimmoNotesData";

    // notes table name
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_CHECKLISTS = "checklists";

    // notes Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_METADATA = "metadata";

    private Context sContext;

    public NotesDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sContext = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT," +
                KEY_CONTENT + " TEXT," +
                KEY_METADATA + " TEXT" +
                ")";
        db.execSQL(CREATE_NOTES_TABLE);
        String CREATE_CHECKLISTS_TABLE = "CREATE TABLE " + TABLE_CHECKLISTS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT," +
                KEY_CONTENT + " TEXT," +
                KEY_METADATA + " TEXT" +
                ")";
        db.execSQL(CREATE_CHECKLISTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLISTS);

        // Create tables again
        onCreate(db);
    }

    // Drop Tables
    public void RecreateTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLISTS);

        // Create tables again
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT," +
                KEY_CONTENT + " TEXT," +
                KEY_METADATA + " TEXT" +
                ")";
        db.execSQL(CREATE_NOTES_TABLE);
        String CREATE_CHECKLISTS_TABLE = "CREATE TABLE " + TABLE_CHECKLISTS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT," +
                KEY_CONTENT + " TEXT," +
                KEY_METADATA + " TEXT" +
                ")";
        db.execSQL(CREATE_CHECKLISTS_TABLE);
        db.close();
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new note
    void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_ID, note.getID());
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, note.getContent());
        values.put(KEY_METADATA, note.getMetadata());

        // Inserting Row
        db.insert(TABLE_NOTES, null, values);
        db.close(); // Closing database connection
    }

    // Getting single note
    Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NOTES, new String[]{KEY_ID,
                        KEY_TITLE, KEY_CONTENT, KEY_METADATA}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            Note note = new Note(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3));
            cursor.close();
            db.close();
            // return note
            return note;
        } else {
            return null;
        }
    }

    // Getting All Notes
    public List<Note> getAllNotes() {
        List<Note> noteList = new ArrayList<>();
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_NOTES;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);

        String order;
        switch (sharedPreferences.getString("note_sorting", "0")) {
            case "0":
                order = KEY_ID + " DESC";
                break;
            case "1":
                order = KEY_ID + " ASC";
                break;
            case "2":
                order = KEY_TITLE + " COLLATE NOCASE DESC";
                break;
            case "3":
                order = KEY_TITLE + " COLLATE NOCASE ASC";
                break;
            default:
                order = KEY_ID + " DESC";
                break;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor cursor = db.rawQuery(selectQuery, null);
        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_METADATA},
                null, null, null, null, order);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setID(Integer.parseInt(cursor.getString(0)));
                note.setTitle(cursor.getString(1));
                note.setContent(cursor.getString(2));
                note.setMetadata(cursor.getString(3));
                // Adding note to list
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return note list
        return noteList;
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, note.getContent());
        values.put(KEY_METADATA, note.getMetadata());

/*
        String strSQL = "UPDATE "
                + TABLE_NOTES + " SET "
                + KEY_TITLE + "='" + note.getTitle() + "', "
                + KEY_CONTENT + "='" + note.getContent() + "', "
                + KEY_METADATA + "='" + note.getMetadata() + "'"
                + " WHERE " + KEY_ID + "='" + note.getID() + "'";
        Log.d("SQL: ", strSQL);
        db.execSQL(strSQL);
*/

        db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.getID())});

        db.close();

        // updating row
//        int ret = db.update(TABLE_NOTES, values, KEY_ID + " = ?",
////                new String[]{String.valueOf(note.getID())});
//                new String[]{String.valueOf(id)});
//        Log.d("TEST", String.valueOf(ret));
//                db.close();
//        Log.d("TEST", String.valueOf(ret));
//        return ret;
    }

    // Deleting single note
    public void deleteNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(note.getID())});
        db.close();
    }


    // Getting notes Count
    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return count;
    }

}