package com.greenorca.shareandcare2024;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.greenorca.shareandcare2024.data.FirebaseShare;
import com.greenorca.shareandcare2024.data.ITopicProvider;
import com.greenorca.shareandcare2024.data.LocalTopicProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * lists firebase share_items,
 * see https://www.zoftino.com/firebase-cloud-firestore-databse-tutorial-android-example
 *
 * has a menu, see https://developer.android.com/training/appbar/setting-up#java
 */
public class MainActivity extends AppCompatActivity {

    private static final int CODE_NODE_ADDED = 1;
    private RecyclerView sharedItemsView;
    private FirebaseAuth mAuth;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseFirestore mFireStore;
    private String currentFilterTopic = "";
    private static final int LOGIN_SETTINGS = 100;
    private ArrayList<FirebaseShare> items = new ArrayList<>();
    private ListenerRegistration registration;
    /*public void openMigrationActivity(View v){
        Intent i = new Intent(this, TestFirebaseActivity.class);
        startActivity(i);
    }    */

    /**
     * creates menu bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * handles menu item clicks
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        final int item_id = item.getItemId();
        switch (item_id) {
            case R.id.action_filter:
                openFilterDialog();
                break;
            case R.id.action_add:
                addNewNode();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * create a filter dialog
     */
    private void openFilterDialog() {
        List<String> topics = new ArrayList<>();
        for (String s : LocalTopicProvider.getInstance(this).getRootTopics()) {
            topics.add(s);
        }
        String[] topicArray = topics.toArray(new String[topics.size()]);
        Log.d(getLocalClassName(), "topics added: "+topicArray.length);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Topic auswählen")
            .setItems(topicArray, (dialog, index) -> {
                currentFilterTopic = topics.get(index);
                fetchData(currentFilterTopic, "");})
            .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    currentFilterTopic = "";
                    fetchData(currentFilterTopic, "");
                }
            })
            ;
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public void onStart() {
        super.onStart();

        login();
    }

    void login(){
        SharedPreferences loginPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
        String user =  loginPrefs.getString("user", null);
        String passwd =  loginPrefs.getString("passwd", null);
        if (user==null || passwd == null){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, LOGIN_SETTINGS);
            return;
        }
        mAuth.signInWithEmailAndPassword(user, passwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        //Toast.makeText(this, "Logged in: "+user.getDisplayName(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //file = new File(getDir("data2", MODE_PRIVATE), "map");
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        sharedItemsView = findViewById(R.id.sharedItemsView);
        sharedItemsView.setHasFixedSize(true);

        LinearLayoutManager linMan = new LinearLayoutManager(this);
        sharedItemsView.setLayoutManager(linMan);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        login();
        initFirestore();

        //fetch all items
        fetchData("","");
        registerSnapshotChangeListeners();

    }

    private void initFirestore(){
        mFireStore = FirebaseFirestore.getInstance();
    }

    private void addNewNode(){
        Intent i = new Intent(this, EditShareActivity.class);
        startActivityForResult(i, CODE_NODE_ADDED);
    }

    private void populateTopics(Spinner spinner){

        ITopicProvider topics = LocalTopicProvider.getInstance(this);
        ArrayAdapter<String> mainTopicsArrayAdapter = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        mainTopicsArrayAdapter.add("");
        for (String t : topics.getRootTopics()) {
            mainTopicsArrayAdapter.add(t);
        }
        spinner.setAdapter(mainTopicsArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String topic = (String)spinner.getSelectedItem();
                Log.d(getLocalClassName(), "selected topic: "+topic);
                fetchData(topic, "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchData("", "");
            }
        });
    }

    @Override
    protected void onActivityResult(int request, int result, Intent i) {
        switch (request){
            case LOGIN_SETTINGS:
                if (result == 1){
                    login();
                }
                break;
            case CODE_NODE_ADDED:
                if (result == 1){
                    fetchData(currentFilterTopic, "");
                    Log.d(getLocalClassName(),"new item received");
                }
                break;
        }

        super.onActivityResult(request, result, i);
    }

    public void onSharedItemsViewClick(View v){
        Log.d(getLocalClassName(), "clicked item: " + v );
    }

    private void populateView(Task<com.google.firebase.firestore.QuerySnapshot> task){
        items.clear();
        for (DocumentSnapshot doc : task.getResult()) {
            FirebaseShare e = doc.toObject(FirebaseShare.class);
            e.setId(doc.getId());
            items.add(e);
        }
        FirebaseShareViewAdapter recyclerViewAdapter = new FirebaseShareViewAdapter(
                items,
                mFireStore,
                MainActivity.this);
        sharedItemsView.setAdapter(recyclerViewAdapter);
        //sharedItemsView.scrollToPosition(items.size() - 1);
    }

    private void removeSnapshotChangeListeners(){
        registration.remove();
    }

    private void registerSnapshotChangeListeners(){
        registration = mFireStore.collection("shared_items").addSnapshotListener((EventListener<QuerySnapshot>) (snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        Log.d(TAG, "new share item: " + dc.getDocument().get("title"));
                        FirebaseShare newElem = dc.getDocument().toObject(FirebaseShare.class);
                        newElem.setId(dc.getDocument().getId());
                        items.add(0,newElem);

                        break;
                    case MODIFIED:
                        Log.d(TAG, "modified share item: " + dc.getDocument().get("title"));
                        String id = dc.getDocument().getId();
                        int index = items.stream().map(item -> item.getUuid())
                                .collect(Collectors.toList())
                                .indexOf(id);
                        FirebaseShare modifiedElem = dc.getDocument().toObject(FirebaseShare.class);
                        items.set(index, modifiedElem);

                        break;
                    case REMOVED:
                        Log.d(TAG, "removed share item: " + dc.getDocument().get("title"));
                        String idRem = dc.getDocument().getId();
                        int indexRem = items.stream().map(item -> item.getUuid())
                                .collect(Collectors.toList())
                                .indexOf(idRem);
                        items.remove(indexRem);
                        break;
                }
                if (sharedItemsView.getAdapter()!=null)
                    sharedItemsView.getAdapter().notifyDataSetChanged();

            }
        });
    }

    /**
     * retrieve shares for given topic or tag
     * @param topic may be empty string
     * @param tag may be empty string
     */
    private void fetchData(String topic, String tag) {
        if (topic.isEmpty() && tag.isEmpty()) {
            mFireStore.collection("shared_items").orderBy("date").limit(250).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            populateView(task);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error getting documents: " + e.getMessage());
                    });

        } else {
            mFireStore.collection("shared_items").whereArrayContains("topics", topic).orderBy("date").limit(250).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            populateView(task);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error getting documents: " + e.getMessage());
                    });
        }
    }

    @Override
    public void onDestroy() {
        removeSnapshotChangeListeners();
        super.onDestroy();
    }
}
