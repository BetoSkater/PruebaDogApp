package com.example.pruebadogapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class AdaptadorRazas extends RecyclerView.Adapter<AdaptadorRazas.ViewHolder> {

    private List<RazaImagen> listaRazaImagen;
    private Context contexto;
    public AdaptadorRazas(List<RazaImagen> listaRazaImagen, Context contexto) {
        this.listaRazaImagen = listaRazaImagen;
        this.contexto = contexto;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.celda_modelo, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AdaptadorRazas.ViewHolder holder, int position) {
        String raza = listaRazaImagen.get(position).getRaza();
        String razaMayusculas = raza.toUpperCase();
        holder.textoRaza.setText(razaMayusculas);

        Uri urlImagen = Uri.parse(listaRazaImagen.get(position).getUrl());
        holder.fotoRaza.setImageURI(urlImagen);
        //Para cargar imagenes dentro de un RecyclerView Glide siempre me ha dado mejores resultados que Picasso.
        Glide.with(contexto).load(urlImagen).into(holder.fotoRaza);

        //Evento onCLick:
        holder.tarjeta.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(contexto, DetalleRaza.class);
                i.putExtra("razaSeleccionada", raza);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Para solventar el error "AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?"

                contexto.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listaRazaImagen.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textoRaza;
        private ImageView fotoRaza;
        private CardView tarjeta;
        public ViewHolder(View itemView) {
            super(itemView);
            textoRaza = (TextView) itemView.findViewById(R.id.lblRaza);
            fotoRaza = (ImageView) itemView.findViewById(R.id.imgImagenRaza);
            tarjeta = (CardView) itemView.findViewById(R.id.crdModelo);
        }
    }
}
