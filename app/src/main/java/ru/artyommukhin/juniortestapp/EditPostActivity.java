package ru.artyommukhin.juniortestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

public class EditPostActivity extends AppCompatActivity {

    public static final String EXTRA_CREATED_POST = "ru.artyommukhin.juniortestapp.extra.CREATED_POST";
    public static final String EXTRA_EDITED_POST = "ru.artyommukhin.juniortestapp.extra.EDITED_POST";

    private Post postToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        EditText postTitle = findViewById(R.id.post_title);
        EditText postBody = findViewById(R.id.post_body);
        EditText userId = findViewById(R.id.user_id);
        Button saveButton = findViewById(R.id.button_save_post);

        setTitle("Создать пост");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            postToEdit = (Post) extras.get(ViewPostActivity.EXTRA_POST_TO_EDIT);
            if (postToEdit != null) {
                setTitle("Редакитровать пост");
                postTitle.setText(postToEdit.getTitle());
                postBody.setText(postToEdit.getBody());
                userId.setText(Integer.toString(postToEdit.getUserId()));
                saveButton.setText("Сохранить");
            }
        }

        saveButton.setOnClickListener(view -> {

            if (postToEdit != null) {

                if (userId.getText().toString().equals(Integer.toString(postToEdit.getUserId()))
                        && postTitle.getText().toString().equals(postToEdit.getTitle())
                        && postBody.getText().toString().equals(postToEdit.getBody())) {

                    Toast.makeText(this, "Пост сохранен без изменений", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Изменение поста
                Post editedPost = new Post(
                        Integer.parseInt(userId.getText().toString()),
                        postToEdit.getId(),
                        postTitle.getText().toString(),
                        postBody.getText().toString()
                );

                Intent editedPostIntent = new Intent();
                editedPostIntent.putExtra(EXTRA_EDITED_POST, editedPost);
                setResult(RESULT_OK, editedPostIntent);
                finish();

            } else {
                // Создание поста
                Post createdPost = new Post(
                        Integer.parseInt(userId.getText().toString()),
                        new Random().nextInt(1000) + 101,
                        postTitle.getText().toString(),
                        postBody.getText().toString()
                );

                Intent createdPostIntent = new Intent();
                createdPostIntent.putExtra(EXTRA_CREATED_POST, createdPost);
                setResult(RESULT_OK, createdPostIntent);
                finish();
            }
        });
    }
}