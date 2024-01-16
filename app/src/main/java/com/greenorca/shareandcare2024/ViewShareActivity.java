package com.greenorca.shareandcare2024;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.greenorca.shareandcare2024.data.FirebaseShare;

public class ViewShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_share);

        Intent i = getIntent();
        FirebaseShare share = (FirebaseShare) i.getSerializableExtra("share");
        assert share != null;

        TextView lblTitle = findViewById(R.id.lblTitle);
        if (share.getUrl()!=null){
            String url = "<a href='"+share.getUrl()+"'>"+share.getTitle()+"</a>";
            lblTitle.setText(Html.fromHtml(url, Html.FROM_HTML_MODE_LEGACY));
            lblTitle.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            lblTitle.setText(share.getTitle());
            lblTitle.setMovementMethod(null);
        }
        TextView lblNotes = findViewById(R.id.lblNotes);
        lblNotes.setText(share.getNotes());

        TextView lblTags = findViewById(R.id.lblTags);
        StringBuilder sb = new StringBuilder();
        for (String tag : share.getTags()) {
            sb.append(tag).append(';');
        }
        lblTags.setText(sb.toString());

        ViewGroup layoutTopics = findViewById(R.id.layoutTopics);
        for (String topic : share.getTopics()) {
            Button b = new Button(this);
            b.setText(topic);
            b.setEnabled(false);
            layoutTopics.addView(b);
        }

    }
}
