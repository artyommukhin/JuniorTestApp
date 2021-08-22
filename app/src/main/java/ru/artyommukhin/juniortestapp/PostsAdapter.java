package ru.artyommukhin.juniortestapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostsAdapter extends ArrayAdapter<Post> {

    ArrayList<Post> postsList;
    private LayoutInflater inflater;
    private int layout;


    public PostsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Post> postsList) {
        super(context, resource);
        this.postsList = postsList;
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Post curPost = getItem(position);

        if (convertView == null)
            convertView = inflater.inflate(this.layout, parent, false);

        TextView title = convertView.findViewById(R.id.list_item_title);
        TextView body = convertView.findViewById(R.id.list_item_body);
        ImageView image = convertView.findViewById(R.id.list_item_image);

        title.setText(curPost.getTitle());
        body.setText(curPost.getBody());
        Picasso.get().load(curPost.getThumbImageUrl()).into(image);

        return convertView;
    }

    @Nullable
    @Override
    public Post getItem(int position) {
        return postsList.get(position);
    }

    @Override
    public int getCount() {
        return postsList.size();
    }

    @Override
    public long getItemId(int position) {
        return postsList.get(position).getId();
    }

    
}
