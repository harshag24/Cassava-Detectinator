package com.example.se_ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;


    public class myadapter extends FirebaseRecyclerAdapter<model,myadapter.myviewholder>
    {
        public myadapter(@NonNull FirebaseRecyclerOptions<model> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull model model)
        {
            holder.prediction.setText(model.getPrediction());
            Glide.with(holder.img.getContext()).load(model.getUrl()).into(holder.img);
        }

        @NonNull
        @Override
        public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerow,parent,false);
            return new myviewholder(view);
        }

        class myviewholder extends RecyclerView.ViewHolder
        {
            ImageView img;
            TextView prediction;
            public myviewholder(@NonNull View itemView)
            {
                super(itemView);
                img= itemView.findViewById(R.id.img1);
                prediction= itemView.findViewById(R.id.predtext);
            }
        }
    }


