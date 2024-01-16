package com.greenorca.shareandcare2024;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.greenorca.shareandcare2024.data.FirebaseShare;

import java.text.DateFormat;
import java.util.List;

public class FirebaseShareViewAdapter extends RecyclerView.Adapter<FirebaseShareViewAdapter.ShareViewHolder> {

    private FirebaseFirestore fireStore;
    private List<FirebaseShare> sharedItems;
    private ViewGroup parent;

    private final Context context;

    /**
     * handles display and events for a single item within the recycler view
     */
    public class ShareViewHolder extends RecyclerView.ViewHolder {
        TextView shareTitle;
        TextView shareSender;
        TextView shareDate;
        FirebaseShare shareItem = null;

        /**
         * ctor
         * @param itemView the layout for this item
         */
        public ShareViewHolder(@NonNull View itemView) {
            super(itemView);
            Context context = itemView.getContext();
            shareTitle = itemView.findViewById(R.id.shareTitle);
            shareSender = itemView.findViewById(R.id.shareSender);
            shareDate = itemView.findViewById(R.id.shareDate);

            Button btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(x ->
                    FirebaseShareViewAdapter.this.removeItem(getAdapterPosition())
                );
            Button btnEdit = itemView.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(x -> {
                    Intent i = new Intent(context, EditShareActivity.class);
                    i.putExtra("editShare", shareItem);
                    context.startActivity(i);
                }
            );

            itemView.setOnClickListener(x -> {
                    Intent i = new Intent(context, ViewShareActivity.class);
                    i.putExtra("share", shareItem);
                    context.startActivity(i);
                }
            );
        }

        public void setSharedItem(FirebaseShare item){
            shareItem = item;
        }
    }

    public FirebaseShareViewAdapter(List<FirebaseShare> items, FirebaseFirestore fireStore, Context context){
        this.fireStore = fireStore;
        this.sharedItems = items;
        this.context = context;
        items.sort((a,b)->{
            return b.getDate().compareTo(a.getDate());
        });
    }

    @NonNull
    @Override
    public FirebaseShareViewAdapter.ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.share_item_view, parent, false);

        ShareViewHolder vh = new ShareViewHolder(view);
        this.parent = parent;
        return vh;
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ShareViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        FirebaseShare currentSharedItem = sharedItems.get(position);
        Log.d(getClass().getSimpleName(), ""+holder+", "+currentSharedItem.getTitle());
        holder.setSharedItem(currentSharedItem);
        if (currentSharedItem.getUrl()!=null){
            String url = "<a href='"+currentSharedItem.getUrl()+"'>"+currentSharedItem.getTitle()+"</a>";
            holder.shareTitle.setText(Html.fromHtml(url, Html.FROM_HTML_MODE_LEGACY));
            holder.shareTitle.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.shareTitle.setText(currentSharedItem.getTitle());
        }
        holder.shareSender.setText(currentSharedItem.getSender());
        if (currentSharedItem.getDate()!=null)
            holder.shareDate.setText(DateFormat.getDateInstance().format(currentSharedItem.getDate()));
    }

    @Override
    public int getItemCount(){
        Log.d(getClass().getSimpleName(), "getItemCount has been called: "+sharedItems.size());
        return sharedItems.size();
    }

    private void removeItem(int position){
        FirebaseShare removeShare = sharedItems.get(position);
        new AlertDialog.Builder(parent.getContext())
            .setTitle("Vorsicht")
            .setMessage("Soll dieser EIntrag unwiederbringlich gelöscht werden?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    fireStore.collection("shared_items").document(removeShare.getUuid())
                            .delete()
                            .addOnCompleteListener(v-> {
                                sharedItems.remove(position);
                                notifyItemRemoved(position);
                            });
                }})
            .setNegativeButton(android.R.string.no, null).show();
    }

}
