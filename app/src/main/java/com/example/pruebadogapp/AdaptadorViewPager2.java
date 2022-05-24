package com.example.pruebadogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdaptadorViewPager2 extends RecyclerView.Adapter<AdaptadorViewPager2.ViewHolder> {

    Context contexto;
    List<String> imagenesAleatorias;

    public AdaptadorViewPager2(Context contexto, List<String> imagenesAleatorias) {
        this.contexto = contexto;
        this.imagenesAleatorias = imagenesAleatorias;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.imagen_carrusel, parent,false);
        ViewHolder viewHolder = new ViewHolder(vista);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(contexto).load(imagenesAleatorias.get(position)).into(holder.imgExpuesta);
    }

    @Override
    public int getItemCount() {
        return imagenesAleatorias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgExpuesta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExpuesta = itemView.findViewById(R.id.imgViewPager);
        }
    }
}
