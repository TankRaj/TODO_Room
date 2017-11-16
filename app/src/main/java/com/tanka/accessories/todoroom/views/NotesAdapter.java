package com.tanka.accessories.todoroom.views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(activity,itemList.get(listPosition).getTitle(),Toast.LENGTH_LONG).show();
        });
        holder.itemView.setOnLongClickListener(v -> {
            deleteItem(itemList.get(listPosition));
            return true;
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView date;
        public TextView body;
        public TextView type;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            body = itemView.findViewById(R.id.tvBody);
            type = itemView.findViewById(R.id.tvType);
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