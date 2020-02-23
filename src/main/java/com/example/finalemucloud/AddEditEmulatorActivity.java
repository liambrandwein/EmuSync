package com.example.finalemucloud;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.finalemucloud.dialogs.DeleteDialog;

import java.io.File;
import java.util.Stack;

public class AddEditEmulatorActivity extends AppCompatActivity implements DeleteDialog.ExampleDialogListener {

    public static final String EXTRA_ID = "com.example.finalemucloud.EXTRA_ID";
    public static final String EXTRA_TITLE = "com.example.finalemucloud.EXTRA_TITLE";
    public static final String EXTRA_DIRECTORY1 = "com.example.finalemucloud.EXTRA_DIRECTORY1";
    public static final String EXTRA_DIRECTORY2 = "com.example.finalemucloud.EXTRA_DIRECTORY2";
    public static final String EXTRA_PACKAGENAME = "com.example.finalemucloud.EXTRA_PACKAGENAME";
    public static final String EXTRA_SYNCTYPE = "com.example.finalemucloud.EXTRA_SYNCTYPE";
    public static final String EXTRA_MAINUPLOAD = "com.example.finalemucloud.EXTRA_MAINUPLOAD";
    public static final String EXTRA_SECONDARYUPLOAD = "com.example.finalemucloud.EXTRA_SECONDARYUPLOAD";
    public static final String EXTRA_PRIORITY = "com.example.finalemucloud.EXTRA_PRIORITY";


    private EditText editTextTitle;
    private EditText editTextDirectory1;
    private EditText editTextDirectory2;
    private EditText editTextPackageName;
    private Switch switchSyncType;
    private CheckBox checkBoxDirectory1;
    private CheckBox checkBoxDirectory2;

    private Button deleteButton;

    private EmulatorViewModel emulatorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emulator);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDirectory1 = findViewById(R.id.edit_text_directory1);
        editTextDirectory2 = findViewById(R.id.edit_text_directory2);
        editTextPackageName = findViewById(R.id.edit_text_packagename);
        switchSyncType = findViewById(R.id.switch_sync);
        checkBoxDirectory1 = findViewById(R.id.checkBox_directory1);
        checkBoxDirectory2 = findViewById(R.id.checkBox_directory2);

        deleteButton = findViewById(R.id.button_delete_emulator);



        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_ID)) {
            setTitle("Edit settings");
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextDirectory1.setText(intent.getStringExtra(EXTRA_DIRECTORY1));
            editTextDirectory2.setText(intent.getStringExtra(EXTRA_DIRECTORY2));
            editTextPackageName.setText(intent.getStringExtra(EXTRA_PACKAGENAME));
            if ((intent.getIntExtra(EXTRA_SYNCTYPE, 2)) == 1) {
                switchSyncType.setChecked(true);
            } else if ((intent.getIntExtra(EXTRA_SYNCTYPE, 2)) == 2) {
                switchSyncType.setChecked(false);
            }
            checkBoxDirectory1.setChecked(intent.getBooleanExtra(EXTRA_MAINUPLOAD, false));
            checkBoxDirectory2.setChecked(intent.getBooleanExtra(EXTRA_SECONDARYUPLOAD, false));
        } else {
            setTitle("Add emulator");
            deleteButton.setVisibility(View.GONE);
        }

    }


    private void saveEmulator() {
        String title = editTextTitle.getText().toString();
        String directory1 = editTextDirectory1.getText().toString();
        String directory2 = editTextDirectory2.getText().toString();
        String packageName = editTextPackageName.getText().toString();

        int syncType;

        if (switchSyncType.isChecked()) {
            syncType = 1;
        } else {
            syncType = 2;
        }
        boolean mainUpload = checkBoxDirectory1.isChecked();
        boolean secondaryUpload = checkBoxDirectory2.isChecked();

        if (title.trim().isEmpty() || directory1.trim().isEmpty()) {
            Toast.makeText(this, "Required field(s) empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (directory2.trim().isEmpty()) {
            directory2 = "";
        }

        if (packageName.trim().isEmpty()) {
            packageName = "";
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_DIRECTORY1, directory1);
        data.putExtra(EXTRA_DIRECTORY2, directory2);
        data.putExtra(EXTRA_PACKAGENAME, packageName);
        data.putExtra(EXTRA_SYNCTYPE, syncType);
        data.putExtra(EXTRA_MAINUPLOAD, mainUpload);
        data.putExtra(EXTRA_SECONDARYUPLOAD, secondaryUpload);

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id != -1) {
            data.putExtra(EXTRA_ID, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    public void deleteEmulator() {
        Intent intent = getIntent();
        emulatorViewModel = ViewModelProviders.of(this).get(EmulatorViewModel.class);
        Emulator emulator = intent.getParcelableExtra("Emulator Item");
        emulatorViewModel.delete(emulator);
        Intent intent2 = new Intent(AddEditEmulatorActivity.this, MainActivity2.class);
        startActivity(intent2);

    }



    public void openDialog(View v) {
        DeleteDialog dialog = new DeleteDialog();
        dialog.show(getSupportFragmentManager(), "delete dialog");
    }

    @Override
    public void onYesClicked() {
        deleteEmulator();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_emulator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_emulator_button:
                saveEmulator();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
