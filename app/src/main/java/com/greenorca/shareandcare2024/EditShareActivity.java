package com.greenorca.shareandcare2024;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.greenorca.shareandcare2024.data.FirebaseShare;
import com.greenorca.shareandcare2024.data.ITopicProvider;
import com.greenorca.shareandcare2024.data.LocalTopicProvider;
import com.greenorca.shareandcare2024.data.ShareFireStorage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * activity to create and update share items
 */
public class EditShareActivity extends AppCompatActivity {

    FirebaseShare inputShare = null;
    EditText txtTags , txtTitle, txtUrl, txtNotes;
    CheckBox chipDone;
    List<String> sharedTopics;
    private ViewGroup mainTopicsLayout;
    private ViewGroup subTopicslayout;
    private String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_receiver);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        sender = prefs.getString("user", "null").split("@")[0];

        txtTags = findViewById(R.id.editTextTags);
        txtTitle = findViewById(R.id.editTextTitle);
        txtUrl = findViewById(R.id.editTextUrl);
        txtNotes = findViewById(R.id.editTextNotizen);
        chipDone = findViewById(R.id.chipDone);

        mainTopicsLayout = findViewById(R.id.mainTopicsLayout);
        subTopicslayout = findViewById(R.id.subTopicsLayout);

        sharedTopics = new ArrayList<>();
        Intent incomingIntent = getIntent();
        String action = incomingIntent.getAction();
        String type = incomingIntent.getType();
        Log.d(getLocalClassName(),"type: "+type + ", action: "+action);
        if (Intent.ACTION_SEND.equals(action) && type.equals("text/plain")){
            final String[] sharedString = {incomingIntent.getStringExtra(Intent.EXTRA_TEXT)};
            inputShare = new FirebaseShare(sharedString[0], sender);
            handleUrl(sharedString);

        } else {
            inputShare = (FirebaseShare)incomingIntent.getSerializableExtra("editShare");
            if (inputShare != null){
                Log.d(getLocalClassName(), "received existing share");
                try {
                    sharedTopics = inputShare.getTopics();
                } catch (Exception ex){
                    sharedTopics = new ArrayList<>();
                }
                txtTitle.setText(inputShare.getTitle());
                txtUrl.setText(inputShare.getUrl());
                txtNotes.setText(inputShare.getNotes());

                StringBuilder tags = new StringBuilder();
                if (inputShare.getTags()!=null) {
                    for (String tag : inputShare.getTags()) {
                        tags.append(tag).append(";");
                    }
                }

                txtTags.setText(tags.toString());
                chipDone.setChecked(!inputShare.isOpen());
            }
            else {
                inputShare = new FirebaseShare("", sender);
                txtTitle.setHint("add something yourself");
            }

        }
        populateTopics();
    }

    /**
     * in case of valid URL, retrieve the page and extract its title
     * @param sharedString
     */
    private void handleUrl(String[] sharedString) {
        if (sharedString[0].startsWith("http://") || sharedString[0].startsWith("https://")){
            (new Thread(() -> {
                try {
                    Document doc  = Jsoup.connect(sharedString[0]).get();
                    String title = doc.title();
                    Log.d(EditShareActivity.class.getSimpleName(),"extracted title: "+title);
                    inputShare.setTitle(title);
                    inputShare.setUrl(sharedString[0]);
                    runOnUiThread(()->{
                        txtTitle.setText(title);
                        txtUrl.setText(sharedString[0]);
                    });

                } catch (Exception ex){
                    Log.e(EditShareActivity.class.getSimpleName(),ex.getMessage());
                }
            })).start();
        } else {
            txtTitle.setText(sharedString[0]);
        }
    }

    public void onSaveButtonClicked(View v){
        inputShare.setTitle(txtTitle.getText().toString());
        inputShare.setUrl(txtUrl.getText().toString());
        inputShare.setOpen(!chipDone.isChecked());
        inputShare.setNotes(txtNotes.getText().toString());
        inputShare.setTags(Arrays.asList(txtTags.getText().toString().split("[,;]")));
        inputShare.setTopics(sharedTopics);
        inputShare.setDate(new Date());
        //leaving the sender as it was!
        ShareFireStorage.getInstance().addShare(inputShare);
        setResult(1);
        finish();

    }

    public void onCancelButtonClicked(View v){
        setResult(0); finish();
    }

    private void populateTopics(){
        ITopicProvider topicProvider = LocalTopicProvider.getInstance(this);
        Set<String> topics = topicProvider.getRootTopics();
        for (String topic: topics) {
            Switch btn = new Switch(this);
            btn.setText(topic);
            btn.setChecked(sharedTopics.contains(topic));
            btn.setOnClickListener(topicSelectionListener);
            mainTopicsLayout.addView(btn);
        };
    }

    View.OnClickListener topicSelectionListener = new View.OnClickListener() {
        public void onClick(View x) {
            String topic = ((Switch) x).getText().toString();
            if (((Switch) x).isChecked()) {
                Log.d(EditShareActivity.class.getSimpleName(),"added topic: "+topic);
                sharedTopics.add(topic);
                subTopicslayout.removeAllViews();
                addSubtopicViews(topic);

            } else {
                Log.d(EditShareActivity.class.getSimpleName(), "removing topic " + topic);
                sharedTopics.remove(topic);
            }
        }
    };

    /**
     * provides new UI elements for subtopics of given topic
     * @param topic
     */
    private void addSubtopicViews(String topic) {
        for (String subtopic :LocalTopicProvider.getInstance(EditShareActivity.this).getSubTopics(topic)){
            Log.d(EditShareActivity.class.getSimpleName(), "adding subtopic: " + subtopic);
            Switch s = new Switch(EditShareActivity.this);
            s.setText(subtopic);
            s.setChecked(sharedTopics.contains(subtopic));
            s.setOnClickListener(innerSwitch -> {
                if (((Switch)innerSwitch).isChecked()){
                    sharedTopics.add(subtopic);
                } else {
                    sharedTopics.remove(subtopic);
                }
            });
            subTopicslayout.addView(s);
        };
    }
}
