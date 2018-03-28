package com.example.studenthelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by DELL on 3/28/2018.
 */

public class Folders extends AppCompatActivity {

    private EditText folderTextField;
    private Button addFolderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folders_page);

        GridView foldersGrid = findViewById(R.id.folder_grid);
        foldersGrid.setAdapter(new FoldersAdapter(this));

        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        final String user_id = prefs.getString("user_id", "null");

        // Folder name
        folderTextField = findViewById(R.id.folder_name_field);

        addFolderBtn = findViewById(R.id.add_folder_btn);

        addFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String folderName = folderTextField.getText().toString().trim();

                try {
                    validateFolderName(folderName);
                } catch (ExceptionHandler e){
                    Toast.makeText(Folders.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("folders/" + user_id);

                // Check if the folder name is already taken
                ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       boolean found = false;
                       for(DataSnapshot folder: dataSnapshot.getChildren()){
                           String foundName = folder.child("folder_name").getValue(String.class);

                           if(foundName.equals(folderName)){
                               found = true;

                               Toast.makeText(Folders.this, "Folder already exists", Toast.LENGTH_LONG).show();

                               break;
                           }
                       }

                       if(!found){
                           // No folder with name exists; Proceed with adding folder
                           addFolder(folderName, ref);
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
            }
        });
    }

    private void validateFolderName(String name) throws ExceptionHandler {

        if(!name.matches("[a-zA-Z0-9 _]*")){
            throw new ExceptionHandler("Folder name cannot contain any special characters other than space/underscore");
        } else if(name.length() > 10){
            throw new ExceptionHandler("Folder name cannot contain more than 10 characters");
        } else if(name.length() < 1){
            throw new ExceptionHandler("Folder needs a better name");
        }
    }

    private void addFolder(String name, DatabaseReference dRef){
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("folder_name", name);

        dRef.push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Folder Added
                if(task.isSuccessful()){
                    Toast.makeText(Folders.this, "Folder added", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Folders.this, "Error adding folder", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

class FoldersAdapter extends BaseAdapter {

    private ArrayList<String> list;
    private Context context;

    class ViewHolder {
        TextView folderName;

        ViewHolder(View v){
            folderName = v.findViewById(R.id.single_folder_name);
        }
    }

    FoldersAdapter(Context context){
        list = new ArrayList<>();
        this.context = context;

        // Get Shared Preferences
        SharedPreferences prefs = context.getSharedPreferences("PREFERENCES", MODE_PRIVATE);

        // Get currently logged in user ID
        String user_id = prefs.getString("user_id", "null");

        // Get reference to the database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("folders/" + user_id);

        ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot folder: dataSnapshot.getChildren()){
                    list.add(folder.child("folder_name").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = null;

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.grid_single_folder_item, viewGroup, false);

            holder = new ViewHolder(row);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.folderName.setText(list.get(i));

        return row;
    }
}