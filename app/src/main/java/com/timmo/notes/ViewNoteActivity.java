package com.timmo.notes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewNoteActivity extends AppCompatActivity implements View.OnClickListener {

    // region Global Vars
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private NotesDatabaseHandler db;
    private List<Note> notes;
    private int identifier;
    private String whichHasFocus;
    private EditText editTextTitle, editTextContent;
    private TextView textViewTitle, textViewContent, textViewMetadata;
    private CardView cardViewEdit;
    private View noteView;
    private Animation animationFadeIn, animationFadeOut;
    private SharedPreferences sharedPreferences;
    // endregion

    // region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (sharedPreferences.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        identifier = getIntent().getExtras().getInt("note_id");
        int notePosition = getIntent().getExtras().getInt("note_pos");

        // startActivityForResult() workaround
        sharedPreferences.edit().putInt("returning_id", notePosition).apply();

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewContent = (TextView) findViewById(R.id.textViewContent);
        textViewMetadata = (TextView) findViewById(R.id.textViewMetadata);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        cardViewEdit = (CardView) findViewById(R.id.cardViewEdit);
        noteView = findViewById(R.id.noteView);

        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        Button buttonSave = (Button) findViewById(R.id.buttonSave);

        db = new NotesDatabaseHandler(ViewNoteActivity.this);
        notes = db.getAllNotes();

        Note note = db.getNote(identifier);

        textViewTitle.setText(note.getTitle());
        textViewContent.setText(note.getContent());
        textViewMetadata.setText(note.getMetadata());

        ImageButton imageButtonEdit = (ImageButton) findViewById(R.id.imageButtonEdit);
        ImageButton imageButtonDelete = (ImageButton) findViewById(R.id.imageButtonDelete);

        imageButtonEdit.setVisibility(View.VISIBLE);
        imageButtonDelete.setVisibility(View.VISIBLE);

        imageButtonEdit.setOnClickListener(this);
        imageButtonDelete.setOnClickListener(this);

        buttonCancel.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

    }

    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }
    // endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(ViewNoteActivity.this, SettingsActivity.class));
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    // region onClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonVoice:
                if (editTextTitle.isFocused()) {
                    whichHasFocus = "title";
                } else if (editTextContent.isFocused()) {
                    whichHasFocus = "content";
                } else {
                    whichHasFocus = "title";
                    editTextTitle.requestFocus();
                }
                promptSpeechInput();
                break;
            case R.id.imageButtonDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewNoteActivity.this);
                builder.setMessage(R.string.text_confirm).setTitle(R.string.text_delete);

                builder.setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.deleteNote(notes.remove(identifier));
                        Toast.makeText(ViewNoteActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putString("return_callback", "deleted").apply();
                        //Intent intent = new Intent(ViewNoteActivity.this, NotesActivity.class);
                        //startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.imageButtonEdit:
                noteView.startAnimation(animationFadeOut);
                animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        noteView.setVisibility(View.GONE);

                        editTextTitle.setText(textViewTitle.getText());
                        editTextContent.setText(textViewContent.getText());

                        cardViewEdit.startAnimation(animationFadeIn);
                        cardViewEdit.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
            case R.id.buttonCancel:
                cardViewEdit.startAnimation(animationFadeOut);
                animationFadeOut.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardViewEdit.setVisibility(View.GONE);

                        editTextContent.clearFocus();
                        editTextTitle.clearFocus();
                        editTextContent.setText("");
                        editTextTitle.setText("");
                        View viewFocus = ViewNoteActivity.this.getCurrentFocus();
                        if (viewFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                        }

                        noteView.startAnimation(animationFadeIn);
                        noteView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
            case R.id.buttonSave:
                cardViewEdit.startAnimation(animationFadeOut);
                animationFadeOut.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardViewEdit.setVisibility(View.GONE);

                        editTextContent.clearFocus();
                        editTextTitle.clearFocus();
                        View viewFocus = ViewNoteActivity.this.getCurrentFocus();
                        if (viewFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                        }

                        textViewTitle.setText(editTextTitle.getText().toString());
                        textViewContent.setText(editTextContent.getText().toString());
                        textViewMetadata.setText("Updated on: " + printStandardDate());

                        //db.deleteNote(notes.remove(identifier));
                        //int id = db.getNotesCount() + 1;
                        //db.addNote(new Note(identifier, editTextTitle.getText().toString(), editTextContent.getText().toString(), "Updated on: " + printStandardDate()));
                        //db.updateNote(notes.set(identifier, new Note(identifier, editTextTitle.getText().toString(), editTextContent.getText().toString(), "Updated on: " + printStandardDate())));
                        db.updateNote(new Note(identifier, editTextTitle.getText().toString(),
                                editTextContent.getText().toString(),
                                "Updated on: " + printStandardDate()));

                        sharedPreferences.edit().putString("return_callback", "edited").apply();

                        noteView.startAnimation(animationFadeIn);
                        noteView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
        }
    }
    // endregion

    // region onBackPressed
    @Override
    public void onBackPressed() {
/*
        Intent intent = new Intent(ViewNoteActivity.this, NotesActivity.class);
        intent.putExtra("back_to_id", identifier);

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (ViewNoteActivity.this, cardViewNote, "cardViewNote");
            startActivity(intent, options.toBundle());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
            overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
        } else {
            startActivity(intent);
*/
        super.onBackPressed();
        //overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
//        }

    }
    //endregion

    // region Speech to Text
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (whichHasFocus.equals("title")) {
                        editTextTitle.append(result.get(0));
                    } else if (whichHasFocus.equals("content")) {
                        editTextContent.append(result.get(0));
                    }
                }
                break;
            }
        }
    }
    //endregion

    // region printStandardDate
    private String printStandardDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa - dd MMM yyyy", Locale.getDefault());
        return sdf.format(cal.getTime());
    }
    // endregion

}
