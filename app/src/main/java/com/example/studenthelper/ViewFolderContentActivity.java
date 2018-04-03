package com.example.studenthelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewFolderContentActivity extends AppCompatActivity {

    private FilesAdapter mFilesAdapter;
    private ArrayList<String> mFilesList;
    private GridView mFilesGrid;
    private String userId;
    private String objectName; // Like quiz name or file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_folder_content);

        // Get user id
        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        userId = prefs.getString("user_id", "null");

        // Get folder id
        Intent intent = getIntent();
        String folder_id = intent.getStringExtra("folder_id");

        // Get reference to the files grid
        mFilesGrid = findViewById(R.id.files_grid);

        // Get reference to the current folder's content
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("folders/" + userId + "/" + folder_id + "/content");

        // Initialize filesList to empty
        mFilesList = new ArrayList<>();

        // Set values to the GridView
        ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot file: dataSnapshot.getChildren()){

                    String fileId = file.getValue(String.class);
                    String key = file.getKey();
                    final String fileType = getFileType(key);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(fileType + "/" + userId + "/" + fileId);

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            objectName = dataSnapshot.child(fileType + "_name").getValue(String.class);

                            // Add name to grid
                            mFilesList.add(objectName);

                            ViewFolderContentActivity.this.mFilesAdapter = new FilesAdapter(ViewFolderContentActivity.this, mFilesList, fileType);
                            ViewFolderContentActivity.this.mFilesGrid.setAdapter(mFilesAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getFileType(String key){
        int separator = key.indexOf("_");

        if(separator != -1){
            // Get the string before '_'
            return key.substring(0, separator);
        }

        return "null";
    }
}


class FilesAdapter extends BaseAdapter {

    private ArrayList<String> mList;
    private Context context;
    private String mFileType;

    class ViewHolder {
        TextView fileName;
        ImageView fileIcon;

        ViewHolder(View v){
            fileName = v.findViewById(R.id.single_file_name);
            fileIcon = v.findViewById(R.id.single_file_icon);
        }
    }

    FilesAdapter(Context context, ArrayList<String> filesList){
        this.context = context;
        this.mList = filesList;
    }

    FilesAdapter(Context context, ArrayList<String> filesList, String fileType){
        this.context = context;
        this.mList = filesList;
        this.mFileType = fileType;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder;

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.grid_single_file_item, viewGroup, false);

            holder = new ViewHolder(row);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        switch (mFileType){
            case "quiz": holder.fileIcon.setImageResource(R.drawable.quiz);
                break;
        }

        holder.fileName.setText(mList.get(i));

        return row;
    }
}