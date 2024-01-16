package com.greenorca.shareandcare2024.data;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShareFireStorage {

    private static ShareFireStorage instance;
    private final FirebaseFirestore db;
    private static final String TAG = ShareFireStorage.class.getSimpleName();
    private static final String COLLECTION = "shared_items";
    private ShareFireStorage(){
        db = FirebaseFirestore.getInstance();
    }

    public static ShareFireStorage getInstance(){
        if (instance == null){
            instance = new ShareFireStorage();
        }
        return instance;
    }

    public void addShare(FirebaseShare share){

        db.collection(COLLECTION).document(share.getUuid()).set(share)
                .addOnSuccessListener(t -> {
                    Log.d(getClass().getSimpleName(), "neuer eintrag gespeichert");
                })
                .addOnFailureListener(e -> {
                    Log.d(getClass().getSimpleName(), e.toString());
                });
    }

}
