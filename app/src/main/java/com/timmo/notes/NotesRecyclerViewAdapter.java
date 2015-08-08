package com.timmo.notes;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {

    private static Context sContext;
    private final ArrayList<Integer> dID;
    private final ArrayList<String> dTitle;
    private final ArrayList<String> dContent;
    private final ArrayList<String> dMetadata;
    //private int lastPosition = -1;

    // Adapter's Constructor
    public NotesRecyclerViewAdapter(Context context, ArrayList<Integer> datasetID, ArrayList<String> datasetTitle, ArrayList<String> datasetContent, ArrayList<String> datasetMetadata) {
        dID = datasetID;
        dTitle = datasetTitle;
        dContent = datasetContent;
        dMetadata = datasetMetadata;
        sContext = context;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public NotesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_note, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);

        holder.textViewTitle.setTag(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //setAnimation(holder.cardViewNote, position);

        holder.textViewTitle.setText("ID=" + dID.get(position) + ", Title=" + dTitle.get(position));
        holder.textViewContent.setText(dContent.get(position));
        holder.textViewMetadata.setText(dMetadata.get(position));

/*
        TypedValue typedValue = new TypedValue();
        sContext.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);
        holder.linearLayoutContainer.setBackgroundResource(typedValue.resourceId);
*/

        holder.textViewContent.setMaxLines(3);

        holder.cardViewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(sContext, ViewNoteActivity.class);
                intent.putExtra("note_id", position);
                intent.putExtra("note_title", dTitle.get(position));
                intent.putExtra("note_content", dContent.get(position));
                intent.putExtra("note_metadata", dMetadata.get(position));

                if (Build.VERSION.SDK_INT >= 21) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                            (((NotesActivity) sContext), holder.cardViewNote, "cardViewNote");
                    sContext.startActivity(intent, options.toBundle());
                } else {
                    sContext.startActivity(intent);
                }
            }
        });
    }

/*
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animationFadeIn = AnimationUtils.loadAnimation(sContext, R.anim.anim_slide_up);
            viewToAnimate.startAnimation(animationFadeIn);
            lastPosition = position;
        }
    }
*/

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dTitle.size();
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final CardView cardViewNote;
        //public final LinearLayout linearLayoutContainer;
        public final TextView textViewTitle;
        public final TextView textViewContent;
        public final TextView textViewMetadata;

        /**
         * Constructor
         *
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);
            cardViewNote = (CardView) v.findViewById(R.id.cardViewNote);
            //linearLayoutContainer = (LinearLayout) v.findViewById(R.id.linearLayoutContainer);
            textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
            textViewContent = (TextView) v.findViewById(R.id.textViewContent);
            textViewMetadata = (TextView) v.findViewById(R.id.textViewMetadata);
        }
    }
}