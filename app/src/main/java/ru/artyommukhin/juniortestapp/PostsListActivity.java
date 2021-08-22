package ru.artyommukhin.juniortestapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class PostsListActivity extends AppCompatActivity {

    public static final String EXTRA_POST = "ru.artyommukhin.juniortestapp.extra.POST";

    private final ArrayList<Post> posts = new ArrayList<>();
    private PostsAdapter postsAdapter;
    RequestQueue requestQueue;


    ActivityResultLauncher<Intent> createPost = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Post createdPost = (Post) data.getExtras().get(EditPostActivity.EXTRA_CREATED_POST);
                        createPost(createdPost);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> viewPost = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Post editedPost = (Post) data.getExtras().get(ViewPostActivity.EXTRA_EDITED_POST);
                        Integer deletedPostId = (Integer) data.getExtras().get(ViewPostActivity.EXTRA_DELETED_POST_ID);

                        if (editedPost != null) {
                            updatePost(editedPost.getId(), editedPost);
                            Toast.makeText(getApplicationContext(), "Post changed", Toast.LENGTH_SHORT).show();
                        }
                        if (deletedPostId != null) {
                            deletePost(deletedPostId);
                            Toast.makeText(getApplicationContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        Button addPostButton = findViewById(R.id.button_add_post);
        ListView postsLV = findViewById(R.id.list_view_posts);
        SwipeRefreshLayout refreshLayout = findViewById(R.id.swipe_refresh_posts);

        postsAdapter = new PostsAdapter(this, R.layout.list_item_post, posts);
        postsLV.setAdapter(postsAdapter);

        requestQueue = Volley.newRequestQueue(this);

        downloadPosts();

        refreshLayout.setOnRefreshListener(() -> {
            downloadPosts();
            refreshLayout.setRefreshing(false);
        });

        postsLV.setOnItemClickListener((adapterView, view, position, id) -> {
            Post post = (Post) adapterView.getItemAtPosition(position);
            Intent viewPostIntent = new Intent(this, ViewPostActivity.class);
            viewPostIntent.putExtra(EXTRA_POST, post);
            viewPost.launch(viewPostIntent);
        });

        addPostButton.setOnClickListener(view -> {
            createPost.launch(new Intent(this, EditPostActivity.class));
        });

    }

    private void downloadPosts() {
        // Загрузка всех постов и картинок
        requestQueue.add(new JsonArrayRequest(
                Request.Method.GET,
                "http://jsonplaceholder.typicode.com/posts",
                null,
                responseArray -> {
                    posts.clear();

                        for (int i = 0; i < responseArray.length(); i++) {
                            Post newPost = null;
                            try {
                                JSONObject jsonPost = responseArray.getJSONObject(i);
                                newPost = new Post(
                                        jsonPost.getInt("userId"),
                                        jsonPost.getInt("id"),
                                        jsonPost.getString("title"),
                                        jsonPost.getString("body")
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            posts.add(newPost);

                            requestQueue.add(new JsonObjectRequest(
                                    Request.Method.GET,
                                    String.format("http://jsonplaceholder.typicode.com/photos/%1d", newPost.getId()),
                                    null,
                                    response -> {

                                        String imageUrl = null;
                                        String thumbImageUrl = null;
                                        Integer imageId = null;
                                        try {
                                            imageUrl = response.getString("url");
                                            thumbImageUrl = response.getString("thumbnailUrl");
                                            imageId = response.getInt("id");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if (imageUrl != null && imageId != null) {
                                            Post post = posts.get(getPostIndexById(imageId));
                                            post.setImageUrl(imageUrl);
                                            post.setThumbImageUrl(thumbImageUrl);
                                            postsAdapter.notifyDataSetChanged();
                                        }
                                    },
                                    error -> Log.e("DownloadPostImages", error.getLocalizedMessage())
                            ));
                        }
                },
                error -> Log.e("DownloadPosts", error.toString())
        ));
    }

    private void deletePost(int id) {
        posts.remove((int) getPostIndexById(id));
        requestQueue.add(new StringRequest(
                Request.Method.DELETE,
                String.format("http://jsonplaceholder.typicode.com/posts/%1d", id),
                response -> {
                    Toast.makeText(getApplicationContext(), "post " + id + " deleted", Toast.LENGTH_SHORT).show();
                    postsAdapter.notifyDataSetChanged();
                    Log.d("deletePost", response);
                },
                error -> Log.e("deletePost", error.toString())));
    }

    private void updatePost(int id, Post updatedPost){

        posts.set(getPostIndexById(id), updatedPost);
        postsAdapter.notifyDataSetChanged();

        Log.d("updatePost", updatedPost.toJSON().toString());

        requestQueue.add(new JsonObjectRequest(
                Request.Method.PUT,
                String.format("http://jsonplaceholder.typicode.com/posts/%1d", id),
                updatedPost.toJSON(),
                response -> Log.d("updatePost", response.toString()),
                error -> Log.e("updatePost", error.toString())
        ));
    }

    private void createPost(Post post){

        posts.add(post);
        postsAdapter.notifyDataSetChanged();

        requestQueue.add(new JsonObjectRequest(
                Request.Method.POST,
                "http://jsonplaceholder.typicode.com/posts",
                post.toJSON(),
                response -> Log.d("createPost", response.toString()),
                error -> Log.d("createPost", error.toString())
        ));
    }



    private Integer getPostIndexById(int id) {
        Integer index = null;
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }
}