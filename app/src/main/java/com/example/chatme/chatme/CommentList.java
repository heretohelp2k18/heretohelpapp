package com.example.chatme.chatme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
public class CommentList extends ArrayAdapter<Messages>{
    private Activity context;
    List<Messages> comments;

    public CommentList(Activity context, List<Messages> comments) {
        super(context, R.layout.commentlist, comments);
        this.context = context;
        this.comments = comments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = context.getLayoutInflater();
//        View listViewItem = inflater.inflate(R.layout.commentlist, null, true);
//
//        TextView textViewName = (TextView) listViewItem.findViewById(R.id.txtName);
//        TextView textViewComment = (TextView) listViewItem.findViewById(R.id.txtComment);
//        ImageView imgPhoto = (ImageView) listViewItem.findViewById(R.id.imgcommentprofile);

        Messages comment = comments.get(position);

        if(comment.getId().equals(UserSessionUtil.getSession(context, "userid")))
        {
            return setUserMessage(comment);
        }
        else
        {
            return setOtherMessage(comment);
        }
    }

    public View setOtherMessage(Messages comment)
    {
        LinearLayout chatbotContainer = new LinearLayout(context);
        chatbotContainer.setOrientation(LinearLayout.VERTICAL);
        TextView botMsg = new TextView(context);
        botMsg.setText(Html.fromHtml(comment.getComment()));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        lp.setMargins(10, 10, 10, 10);
        botMsg.setLayoutParams(lp);
        botMsg.setBackgroundResource(R.drawable.bgroundedchatleft);

        GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
        bgdrawable.setColor(Color.parseColor("#DDDDDD"));
        chatbotContainer .addView(botMsg);
        return chatbotContainer;
    }

    public View setUserMessage(Messages comment)
    {
        LinearLayout chatbotContainer = new LinearLayout(context);
        chatbotContainer.setOrientation(LinearLayout.VERTICAL);
        TextView botMsg = new TextView(context);
        botMsg.setText(Html.fromHtml(comment.getComment()));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT;
        lp.setMargins(10, 10, 10, 10);
        botMsg.setLayoutParams(lp);
        botMsg.setBackgroundResource(R.drawable.bgroundedchatright);
        botMsg.setTextColor(Color.parseColor("#FFFFFF"));
        GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
        bgdrawable.setColor(Color.parseColor("#3498db"));
        botMsg.setGravity(Gravity.RIGHT);
        chatbotContainer .addView(botMsg);
        return chatbotContainer;
    }
}
