package com.example.pruebadogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class CarruselAdapter extends PagerAdapter {

    String[] imagenesAleatorias;
    Context contexto;
    LayoutInflater layoutInflater;

    public CarruselAdapter(String[] imagenesAleatorias, Context contexto, LayoutInflater layoutInflater) {
        this.imagenesAleatorias = imagenesAleatorias;
        this.contexto = contexto;
        this.layoutInflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imagenesAleatorias.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
