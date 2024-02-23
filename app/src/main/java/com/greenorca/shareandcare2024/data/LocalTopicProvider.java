package com.greenorca.shareandcare2024.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.greenorca.shareandcare2024.R;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocalTopicProvider implements ITopicProvider {

    private SharedPreferences prefs = null;
    private Context context;
    private Set<String> rootTopics;
    private static LocalTopicProvider instance;
    private static String SHARE_TOPIC = "TOPICS";
    private boolean reset = true;

    private LocalTopicProvider(Context context){
        this.context = context;
        prefs = context.getSharedPreferences(SHARE_TOPIC, Context.MODE_PRIVATE);
        rootTopics = prefs.getAll().keySet();
        if (rootTopics.isEmpty() || reset){
            Log.d(getClass().getSimpleName(),"initializing empty root topic set");
            rootTopics = populateTopics();
        }
    }

    public static ITopicProvider getInstance(Context context){
        if (instance == null){
            instance = new LocalTopicProvider(context);
        }
        return instance;
    }


    @Override
    public Set<String> getRootTopics() {
        return rootTopics;
    }

    @Override
    public Set<String> getSubTopics(String topic) {
        Set<String> result = prefs.getStringSet(topic, new HashSet<String>());
        return result;
    }

    /**
     * absolute basic topics, based on fixed array
     * @return initial set of topics
     */
    private Set<String> populateTopics() {
        SharedPreferences.Editor editor = getEditor();
        Set<String> result = new HashSet<>();
        String[] rootTopics = context.getResources().getStringArray(R.array.root_topics);
        for (String topic : rootTopics){
            result.add(topic);
            Set<String> items = new HashSet<>();
            if (topic.equals("Einkaufen")){
                for (String einkaufen : context.getResources().getStringArray(R.array.einkaufen)){
                    items.add(einkaufen);
                }
            }
            else if (topic.equals("Wandern") || topic.equals("Velo")){
                items.add("Trailfood");
            }
            editor.putStringSet(topic,items);
        };
        editor.commit();
        return result;
    }

    @Override
    public void addTopic(String topic, String parent) {
        SharedPreferences.Editor editor = getEditor();
        Set<String> subtopics = context.getSharedPreferences(SHARE_TOPIC, Context.MODE_PRIVATE).getStringSet(parent, new HashSet<String>());
        subtopics.add(topic);
        editor.putStringSet(parent, subtopics);
        editor.commit();
    }

    private SharedPreferences.Editor getEditor(){
        return context.getSharedPreferences(SHARE_TOPIC, Context.MODE_PRIVATE).edit();
    }
}
