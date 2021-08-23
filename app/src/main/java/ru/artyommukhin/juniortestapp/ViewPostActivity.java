package ru.artyommukhin.juniortestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ViewPostActivity extends AppCompatActivity {

    public static final String EXTRA_EDITED_POST = "ru.artyommukhin.juniortestapp.extra.EDITED_POST";
    public static final String EXTRA_DELETED_POST_ID = "ru.artyommukhin.juniortestapp.extra.DELETED_POST_ID";

    public static final String EXTRA_POST_TO_EDIT = "ru.artyommukhin.juniortestapp.extra.POST_TO_EDIT";

    private Post post;

    ActivityResultLauncher<Intent> editPost = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Post editedPost = (Post) data.getExtras()
                                .get(EditPostActivity.EXTRA_EDITED_POST);

                        Log.d("viewPost", editedPost.toString());

                        Intent editedPostIntent = new Intent();
                        editedPostIntent.putExtra(EXTRA_EDITED_POST, editedPost);
                        setResult(RESULT_OK, editedPostIntent);
                        finish();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        ImageView postImage = findViewById(R.id.post_image);
        TextView postTitle = findViewById(R.id.post_title);
        TextView postBody = findViewById(R.id.post_body);

        Intent intent = getIntent();
        post = (Post) intent.getExtras().get(PostsListActivity.EXTRA_POST);

        setTitle("View post");

        postTitle.setText(post.getTitle());
        postBody.setText(post.getBody());

        Picasso.get().load(post.getImageUrl()).into(postImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit_post:
                Intent editPostIntent = new Intent(this, EditPostActivity.class);
                editPostIntent.putExtra(EXTRA_POST_TO_EDIT, post);
                editPost.launch(editPostIntent);
                return true;

            case R.id.action_delete_post:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialog, id) -> {
                        setResult(RESULT_OK, new Intent().putExtra(EXTRA_DELETED_POST_ID, post.getId()));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                builder.create().show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

