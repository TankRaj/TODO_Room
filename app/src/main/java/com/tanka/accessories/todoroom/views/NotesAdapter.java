package com.tanka.accessories.todoroom.views;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;

import java.util.List;

/**
 * Created by access-tanka on 11/16/17.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private List<Note> itemList;
    MainActivity activity;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;

    public NotesAdapter(MainActivity activity, List<Note> itemList) {
        this.itemList = itemList;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        holder.title.setText(itemList.get(listPosition).getTitle());
        holder.date.setText(itemList.get(listPosition).getDate());
        holder.body.setText(itemList.get(listPosition).getBody());
        holder.type.setText(itemList.get(listPosition).getType());

        AnimationDrawable animationDrawable = (AnimationDrawable) holder.contentLayout.getBackground();
        if (listPosition%2==0){
            animationDrawable.setEnterFadeDuration(2500);
            animationDrawable.setExitFadeDuration(2500);
        }else {
            animationDrawable.setEnterFadeDuration(5000);
            animationDrawable.setExitFadeDuration(10000);
        }

        animationDrawable.start();

        holder.itemView.setOnClickListener(v -> {
//            activity.showCustomToast(itemList.get(listPosition).getTitle());
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                activity.editNote(itemList.get(listPosition));
            } else {
                activity.showCustomToast(itemList.get(listPosition).getTitle());
            }
            lastClickTime = clickTime;

        });
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(listPosition);
//            deleteItem(itemList.get(listPosition));
            return true;
        });
    }

    private void showDeleteDialog(int listPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.text_delete);
        builder.setMessage(R.string.info_delete);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {

            deleteItem(itemList.get(listPosition));
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView date;
        public TextView body;
        public TextView type;
        private LinearLayout contentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            body = itemView.findViewById(R.id.tvBody);
            type = itemView.findViewById(R.id.tvType);
            contentLayout = itemView.findViewById(R.id.contentLayout);

        }

        @Override
        public void onClick(View view) {
            Log.d("onclick", "onClick " + getLayoutPosition() + " " + title.getText());
        }


    }
    private void deleteItem(Note item) {
        activity.deleteNote(item);

    }


}