package com.timmo.notes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

public class NotesActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    // region Global Vars
    private final String descendID = "descend_id";
    private final String ascendID = "ascend_id";
    private final int REQ_CODE_SPEECH_INPUT = 100;

    public RecyclerView.Adapter adapter;
    private String whichHasFocus;
    private EditText editTextTitle, editTextContent;
    private ScrollView scrollViewAddContent;
    private CardView cardViewAdd;
    private ImageButton imageButtonVoice;
    private ArrayList<Integer> arrayListID;
    private ArrayList<String> arrayListTitle, arrayListContent, arrayListMetadata;
    private GridLayoutManager gridLayoutManager;
    private Resources resources;
    private NotesDatabaseHandler db;
    private View viewFocus;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    // endregion

    // region getSpanCount
    private int getSpanCount() {
        //String toastMsg;
        int spanCount = 1;
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            switch (screenOrientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    spanCount = 2;
                    //toastMsg = "LARGE OR GREATER PORTRAIT";
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    spanCount = 3;
                    //toastMsg = "LARGE OR GREATER LANDSCAPE";
                    break;
                default:
                    spanCount = 2;
                    //toastMsg = "LARGE OR GREATER OTHER";
            }
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            switch (screenOrientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    spanCount = 1;
                    //toastMsg = "NORMAL PORTRAIT";
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    spanCount = 2;
                    //toastMsg = "NORMAL LANDSCAPE";
                    break;
                default:
                    spanCount = 1;
                    //toastMsg = "NORMAL OTHER";
            }
        }
        //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
        return spanCount;
    }
    // endregion

    //region getNotesCount
    private int getNotesCount() {
        if (db.getNotesCount() <= 0) {
            return 0;
        } else {
            return db.getNotesCount() - 1;
        }
    }
    //endregion

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
        setContentView(R.layout.activity_notes);

        resources = getResources();

        db = new NotesDatabaseHandler(this);

        scrollViewAddContent = (ScrollView) findViewById(R.id.scrollViewAddContent);
        cardViewAdd = (CardView) findViewById(R.id.cardViewAdd);
        imageButtonVoice = (ImageButton) findViewById(R.id.imageButtonVoice);
        Button buttonAdd = (Button) findViewById(R.id.buttonAdd);
        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);

        editTextTitle.setOnFocusChangeListener(this);
        editTextContent.setOnFocusChangeListener(this);
        imageButtonVoice.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        recyclerView.setHasFixedSize(true);

        // gridLayoutManager(Context this, SpanCount spancount, Orientation 1/portrait, ReverseLayout true);
        gridLayoutManager = new GridLayoutManager(this, getSpanCount(), 1, false);
        //gridLayoutManager.setReverseLayout(true);
        //gridLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Data set used by the adapter. This data will be displayed.
        arrayListID = new ArrayList<>();
        arrayListTitle = new ArrayList<>();
        arrayListContent = new ArrayList<>();
        arrayListMetadata = new ArrayList<>();

        // Create the adapter
        adapter = new NotesRecyclerViewAdapter(NotesActivity.this, arrayListID, arrayListTitle, arrayListContent, arrayListMetadata);
        LoadNotes();

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerViewScrollListener() {
            @Override
            public void onHide() {
                if (scrollViewAddContent.getVisibility() != View.VISIBLE) {
                    hideViews();
                }
            }

            @Override
            public void onShow() {
                showViews();
            }
        });
        recyclerView.setItemAnimator(new FadeInUpAnimator());
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);

        // region First Launch
        SharedPreferences sharedPreferencesFirst = getSharedPreferences("PREFS_FIRST_LAUNCH", 0);
        if (sharedPreferencesFirst.getBoolean(resources.getString(R.string.prefs_note_first_launch), true)) {
//            int id = getNotesCount() + 1;
//            db.addNote(new Note(id, resources.getString(R.string.welcome_note_title),
//                    resources.getString(R.string.welcome_note_content),
//                    "Added on: " + printStandardDate()));
            AddNote(resources.getString(R.string.welcome_note_title), resources.getString(R.string.welcome_note_content));
            sharedPreferencesFirst.edit().putBoolean(resources.getString(R.string.prefs_note_first_launch), false).apply();
        }
        // endregion

        // Google Now
        //TODO GOOGLE NOW!!!
/*
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            editTextContent.setText(query);
            editTextTitle.requestFocus();
        }
*/
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // Google Now
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    editTextContent.setText(sharedText);
                    editTextTitle.requestFocus();
                    imageButtonVoice.performClick();
                }
            }
        } else {
            if (("com.google.android.gm.action.AUTO_SEND").equals(action) && type != null) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    editTextContent.setText(sharedText);
                    editTextTitle.requestFocus();
                    imageButtonVoice.performClick();
                }
            }
        }

        intent.getExtras();
        int backToID = intent.getIntExtra("returning_id", 0);
        if (backToID < 0) {
            backToID = 0;
        }
        gridLayoutManager.smoothScrollToPosition(recyclerView, null, backToID);

    }
    // endregion

    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*
            case R.id.action_lists:
                startActivity(new Intent(NotesActivity.this, NotesActivity.class));
                break;
            case R.id.action_checklists:
                startActivity(new Intent(NotesActivity.this, ChecklistsActivity.class));
                break;
*/
            case R.id.action_settings:
                startActivity(new Intent(NotesActivity.this, SettingsActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    // region Speech to Text
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (whichHasFocus.equals("title")) {
                        editTextContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        editTextTitle.append(result.get(0));
                        editTextContent.requestFocus();
                        imageButtonVoice.performClick();
                    } else if (whichHasFocus.equals("content")) {
                        editTextContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        editTextContent.append(result.get(0));
                    }
                }
                break;
            }
        }
    }
    // endregion

    // region Focus Changed
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.editTextTitle:
                if (editTextTitle.isFocused() || editTextContent.isFocused()) {

                    editTextTitle.setHint(resources.getString(R.string.hint_title));

                    //Animation animationSlideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
                    //scrollViewAddContent.startAnimation(animationSlideDown);
                    //scrollViewAddContent.setVisibility(View.VISIBLE);

                    // Prepare the View for the animation
                    scrollViewAddContent.setVisibility(View.VISIBLE);
                    scrollViewAddContent.setAlpha(0.0f);

                    // Start the animation
                    scrollViewAddContent.animate()
                            //.translationY(scrollViewAddContent.getHeight())
                            .alpha(1.0f);

                    //recyclerView.setPadding(0, linearLayoutAdd.getHeight() + recyclerView.getPaddingTop() + 80, 0, 0);
                    //gridLayoutManager.smoothScrollToPosition(recyclerView, null, getNotesCount());
                } else {
                    editTextTitle.setHint(resources.getString(R.string.hint_title));
                    scrollViewAddContent.setVisibility(View.GONE);
                    //recyclerView.setPadding(0, 210, 0, 0);
                    //gridLayoutManager.smoothScrollToPosition(recyclerView, null, getNotesCount());
                }
                break;
            case R.id.editTextContent:
                if (editTextTitle.isFocused() || editTextContent.isFocused()) {
                    scrollViewAddContent.setVisibility(View.VISIBLE);
                } else {
                    scrollViewAddContent.setVisibility(View.GONE);
                }
                break;
        }
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
            case R.id.buttonCancel:
                editTextContent.clearFocus();
                editTextTitle.clearFocus();
                editTextContent.setText("");
                editTextTitle.setText("");
                editTextTitle.setHint(resources.getString(R.string.hint_add));
                viewFocus = this.getCurrentFocus();
                if (viewFocus != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                }
                break;
            case R.id.buttonAdd:
                if (editTextTitle.getText().toString().equals("") && editTextContent.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Nothing entered..", Toast.LENGTH_SHORT).show();
                } else if (editTextTitle.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesActivity.this);
                    builder.setMessage(R.string.text_empty_confirm).setTitle(R.string.text_empty_title);

                    builder.setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddNote(editTextTitle.getText().toString(),
                                    editTextContent.getText().toString());
                        }
                    });
                    builder.setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (editTextContent.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesActivity.this);
                    builder.setMessage(R.string.text_empty_confirm).setTitle(R.string.text_empty_content);

                    builder.setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddNote(editTextTitle.getText().toString(),
                                    editTextContent.getText().toString());
                        }
                    });
                    builder.setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AddNote(editTextTitle.getText().toString(),
                            editTextContent.getText().toString());
                }
                break;
        }
    }
    // endregion

    // region onResume
    @Override
    protected void onResume() {
        super.onResume();
        String returnTo = sharedPreferences.getString("return_callback", "nothing");
        if (returnTo.equals("edited")) {
            //adapter.notifyItemRemoved(sharedPreferences.getInt("returning_id", 0));
            //gridLayoutManager.smoothScrollToPosition(recyclerView, null, getNotesCount() - 1);
            //adapter.notifyItemInserted(getNotesCount() - 1);
            adapter.notifyItemChanged(sharedPreferences.getInt("returning_id", getNotesCount()));
        } else if (returnTo.equals("deleted")) {
            adapter.notifyItemRemoved(sharedPreferences.getInt("returning_id", getNotesCount()));
        }
        LoadNotes();
    }
    // endregion

    // region onBackPressed
    @Override
    public void onBackPressed() {
        if (editTextTitle.isFocused() || editTextContent.isFocused()) {
            editTextTitle.clearFocus();
            editTextContent.clearFocus();
            editTextContent.setText("");
            editTextTitle.setText("");
            editTextTitle.setHint(resources.getString(R.string.hint_add));
        } else {
            super.onBackPressed();
            //overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
        }
    }
    // endregion

    // region Notes
    private void AddNote(String newTitle, String newContent) {
        // Get
        String newMetadata = "Added on: " + printStandardDate();

        // Set
        //arrayListTitle.add(newID, newTitle);
        //arrayListContent.add(newID, newContent);
        //arrayListMetadata.add(newID, newMetadata);
        //adapter.notifyItemInserted(newID);
        //gridLayoutManager.smoothScrollToPosition(recyclerView, null, newID);

        // Clear
        viewFocus = NotesActivity.this.getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
        }
        editTextContent.setText("");
        editTextTitle.setText("");
        editTextTitle.setHint(resources.getString(R.string.hint_add));
        editTextContent.clearFocus();
        editTextTitle.clearFocus();
        //LoadNotes();
        showViews();

        // Save
        int newPermID = db.getNotesCount() + 1;
        db.addNote(new Note(newPermID, newTitle, newContent, newMetadata));
        LoadNotes();

        //Note newNote = db.getNote(newPermID);

        switch (sharedPreferences.getString("note_sorting", "0")) {
            case "0":
                adapter.notifyItemInserted(arrayListID.indexOf(newPermID));
                break;
            case "1":
                adapter.notifyItemInserted(arrayListID.indexOf(newPermID));
                break;
            case "2":
                adapter.notifyItemInserted(arrayListTitle.indexOf(newTitle));
                break;
            case "3":
                adapter.notifyItemInserted(arrayListTitle.indexOf(newTitle));
                break;
        }
    }

    public void LoadNotes() {
        // Clear existing ArrayList's
        arrayListID.clear();
        arrayListTitle.clear();
        arrayListContent.clear();
        arrayListMetadata.clear();
        // Reading all notes
        List<Note> notes = db.getAllNotes();
        for (Note cn : notes) {
            arrayListID.add(cn.getID());
            arrayListTitle.add(cn.getTitle());
            arrayListContent.add(cn.getContent());
            arrayListMetadata.add(cn.getMetadata());
        }
    }
    // endregion

    // region printStandardDate
    private String printStandardDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa - dd MMM yyyy", Locale.getDefault());
        return sdf.format(cal.getTime());
    }
    // endregion

    // region Animated Views Manipulation
    private void hideViews() {
        if (getNotesCount() + 1 >= 5 && !scrollViewAddContent.isShown()) {
            //toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
            cardViewAdd.animate().translationY(-200).setInterpolator(new AccelerateInterpolator(2));

            //FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
            //int fabBottomMargin = lp.bottomMargin;
            //mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }

    private void showViews() {
        //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        cardViewAdd.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        //mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }
    // endregion

}