package com.tanka.accessories.todoroom.views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import androidx.core.view.MotionEventCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;
import com.tanka.accessories.todoroom.views.helper.ItemTouchHelperAdapter;
import com.tanka.accessories.todoroom.views.helper.ItemTouchHelperViewHolder;
import com.tanka.accessories.todoroom.views.helper.OnStartDragListener;

import java.util.Collections;
import java.util.List;

/**
 * Created by access-tanka on 11/16/17.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private List<Note> itemList;
    MainActivity activity;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private long lastClickTime = 0;
    private final OnStartDragListener mDragStartListener;

    public NotesAdapter(MainActivity activity, List<Note> itemList,OnStartDragListener dragStartListener) {
        this.itemList = itemList;
        this.activity = activity;
        mDragStartListener = dragStartListener;
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
        if (listPosition % 2 == 0) {
            animationDrawable.setEnterFadeDuration(2500);
            animationDrawable.setExitFadeDuration(2500);
        } else {
            animationDrawable.setEnterFadeDuration(5000);
            animationDrawable.setExitFadeDuration(10000);
        }

        animationDrawable.start();

        holder.itemView.setOnClickListener(v -> {

            Note note = itemList.get(listPosition);
            Intent intent = new Intent(activity, NoteDetail.class);
            intent.putExtra("note", note);
            activity.startActivity(intent);

        });
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(listPosition);
            return true;
        });

        holder.ivEdit.setOnClickListener(v -> {
            activity.editNote(itemList.get(listPosition));
        });

        // Start a drag whenever the handle view it touched
//        holder.title.setOnTouchListener((v, event) -> {
//            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
//                mDragStartListener.onStartDrag(holder);
//            }
//            return false;
//        });
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(itemList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,ItemTouchHelperViewHolder {
        public TextView title;
        public TextView date;
        public TextView body;
        public TextView type;
        public ImageView ivEdit;
        private LinearLayout contentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            body = itemView.findViewById(R.id.tvBody);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            type = itemView.findViewById(R.id.tvType);
            contentLayout = itemView.findViewById(R.id.contentLayout);

        }

        @Override
        public void onClick(View view) {
            Log.d("onclick", "onClick " + getLayoutPosition() + " " + title.getText());
        }


        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    private void deleteItem(Note item) {
        activity.deleteNote(item);

    }


}