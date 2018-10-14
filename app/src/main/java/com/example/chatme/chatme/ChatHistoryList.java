package com.example.chatme.chatme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChatHistoryList extends ArrayAdapter<ChatHistory>{
    private Activity context;
    List<ChatHistory> items;
    public ChatHistoryList(Activity context, List<ChatHistory> items) {
        super(context, R.layout.chat_history_list, items);

        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.chat_history_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.txtName);
        TextView textViewDate= (TextView) listViewItem.findViewById(R.id.txtDate);
        ImageView imgPhoto = (ImageView) listViewItem.findViewById(R.id.imgprofile);

        ChatHistory chatHistoryItem = items.get(position);
        textViewName.setText(chatHistoryItem.getChatmate());
        textViewDate.setText(chatHistoryItem.getChatdate());

        Bitmap bitmap;
        if (chatHistoryItem.getGender().equals("Male")) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boy);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.girl);
        }

        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundDrawable.setCircular(true);
        imgPhoto.setImageDrawable(roundDrawable);

        return listViewItem;
    }
}
