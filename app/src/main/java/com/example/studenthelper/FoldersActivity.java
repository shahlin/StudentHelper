package com.example.studenthelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

/**
 * Created by DELL on 3/28/2018.
 */

public class FoldersActivity extends AppCompatActivity {

    private EditText mFolderTextField;
    private Button mAddFolderBtn;
    private FoldersAdapter mFoldersAdapter;
    private ArrayList<String> mFoldersList;
    private GridView mFoldersGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders_list);

        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        final String user_id = prefs.getString("user_id", "null");

        // Get GridView reference
        mFoldersGrid = findViewById(R.id.folder_grid);

        // Get reference to the database
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("folders/" + user_id);

        mFoldersList = new ArrayList<>();

        // Set values to the GridView
        ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot folder: dataSnapshot.getChildren()){
                    mFoldersList.add(folder.child("folder_name").getValue(String.class));
                    FoldersActivity.this.mFoldersAdapter = new FoldersAdapter(FoldersActivity.this, mFoldersList);
                    FoldersActivity.this.mFoldersGrid.setAdapter(mFoldersAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Folder name
        mFolderTextField = findViewById(R.id.folder_name_field);

        // Add Folder Button
        mAddFolderBtn = findViewById(R.id.add_folder_btn);

        mAddFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String folderName = mFolderTextField.getText().toString().trim();

                try {
                    validateFolderName(folderName);
                } catch (ExceptionHandler e){
                    Toast.makeText(FoldersActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                // Check if the folder name is already taken
                ref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean found = false;
                        for(DataSnapshot folder: dataSnapshot.getChildren()){
                            String foundName = folder.child("folder_name").getValue(String.class);

                            if(foundName.equals(folderName)){
                                found = true;

                                Toast.makeText(FoldersActivity.this, "Folder already exists", Toast.LENGTH_LONG).show();

                                break;
                            }
                        }

                        if(!found){
                            // No folder with name exists; Proceed with adding folder
                            addFolder(folderName, ref);

                            if(mFoldersList.isEmpty()){
                                FoldersActivity.this.mFoldersAdapter = new FoldersAdapter(FoldersActivity.this, mFoldersList);
                                FoldersActivity.this.mFoldersGrid.setAdapter(mFoldersAdapter);
                            }

                            mFoldersList.add(folderName);
                            mFoldersAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }); // End of addListenerForSingleValueEvent()
            }
        }); // End of Add Folder Button Click Listener

        mFoldersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String folderClicked = mFoldersGrid.getItemAtPosition(i).toString();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot folder: dataSnapshot.getChildren()){
                            DataSnapshot folder_names = folder.child("folder_name");

                            if(folder_names.getValue(String.class).equals(folderClicked)){
                                String id = folder.getKey();

                                Intent ViewFolderPage = new Intent(FoldersActivity.this, ViewFolderContentActivity.class);
                                ViewFolderPage.putExtra("folder_id", id);

                                startActivity(ViewFolderPage);

                                break;
                            }
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
        } else if(name.length() > 15){
            throw new ExceptionHandler("Folder name cannot contain more than 15 characters");
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
                    Toast.makeText(FoldersActivity.this, "Folder added", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FoldersActivity.this, "Error adding folder", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void logout(){
        SharedPreferences PREFS = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = PREFS.edit();

        editor.clear();
        editor.apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.logout_option:
                logout();

                Intent loginPage = new Intent(this, MainActivity.class);
                loginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginPage);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

class FoldersAdapter extends BaseAdapter {

    private ArrayList<String> mList;
    private Context context;

    class ViewHolder {
        TextView folderName;

        ViewHolder(View v){
            folderName = v.findViewById(R.id.single_folder_name);
        }
    }

    FoldersAdapter(Context context, ArrayList<String> foldersList){
        this.context = context;
        this.mList = foldersList;
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
            row = inflater.inflate(R.layout.grid_single_folder_item, viewGroup, false);

            holder = new ViewHolder(row);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.folderName.setText(mList.get(i));

        return row;
    }
}