package com.greenorca.shareandcare2024;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class TestFirebaseActivity extends AppCompatActivity {

    FirebaseFirestore mFireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_firebase);
        initFirestore();
    }

    private void initFirestore(){
        mFireStore = FirebaseFirestore.getInstance();
    }

    /*public void saveAll2Firebase(View v){
        File file = file = new File(getDir("data2", MODE_PRIVATE), "map");
        List<Share> shareditems = ShareStorage.getInstance(file).read();
        Log.d(getLocalClassName(),"fetched shared items: "+shareditems.size());
        shareditems.forEach( item -> {
            Log.d(getLocalClassName(),"adding shared item: "+item.getTitle());
            Map<String, Object> mappeditem = new HashMap<>();
            mappeditem.put("title", item.getTitle());
            mappeditem.put("url", item.getUrl());
            HashSet<String> topicSet = (HashSet<String>) item.getProperties().get("topics");
            List<String> topics = topicSet.stream().collect(Collectors.toList());
            mappeditem.put("topics", topics);
            mappeditem.put("sender", item.getSender().toString());
            mappeditem.put("created", item.getDate());
            mFireStore.collection("shared_items")
                    .document().set(mappeditem)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(getLocalClassName(),"added shared item: "+item.getTitle());
                    })
                    .addOnFailureListener(e -> {
                        Log.d(getLocalClassName(),"failed to add item: "+e.toString());
                    });
                });

    }*/


}
