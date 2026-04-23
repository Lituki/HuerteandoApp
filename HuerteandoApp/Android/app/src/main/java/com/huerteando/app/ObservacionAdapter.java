package com.huerteando.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.clases.Observacion;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter para el RecyclerView de observaciones
 * 
 * ¿Qué hace esta clase?
 * Se encarga de mostrar cada observación en una "tarjeta" de la lista.
 * Convierte cada objeto Observacion en una vista visual.
 */
public class ObservacionAdapter extends RecyclerView.Adapter<ObservacionAdapter.ViewHolder> {

    private final List<Observacion> observaciones;
    private final OnObservacionClickListener listener;

    // Interface para manejar clicks en las tarjetas
    public interface OnObservacionClickListener {
        void onObservacionClick(Observacion observacion);
    }

    public ObservacionAdapter(List<Observacion> observaciones, OnObservacionClickListener listener) {
        this.observaciones = observaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crear la vista para cada tarjeta
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_observacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener la observación de esta posición
        Observacion obs = observaciones.get(position);
        
        //绑定数据到视图 (asignar datos a la vista)
        holder.bind(obs);
    }

    @Override
    public int getItemCount() {
        return observaciones.size();
    }

    /**
     * Clase interna que representa cada tarjeta de la lista
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView ivImagen;
        private final TextView tvTitulo;
        private final TextView tvTipo;
        private final TextView tvZona;
        private final TextView tvFecha;
        private final TextView tvLikes;
        private final TextView tvComentarios;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Conectar con los elementos del layout
            ivImagen = itemView.findViewById(R.id.ivImagen);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvZona = itemView.findViewById(R.id.tvZona);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComentarios = itemView.findViewById(R.id.tvComentarios);

            // Click en la tarjeta
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onObservacionClick(observaciones.get(position));
                }
            });
        }

        public void bind(Observacion obs) {
            // Título
            tvTitulo.setText(obs.getTitulo());
            
            // Tipo (con color según el tipo)
            tvTipo.setText(obs.getTipoObservacion());
            int colorTipo = getColorTipo(obs.getTipoObservacion());
            tvTipo.setBackgroundColor(colorTipo);
            
            // Zona
            tvZona.setText(obs.getNombreZona() != null ? obs.getNombreZona() : "Sin zona");
            
            // Fecha
            tvFecha.setText(obs.getFechaObservacion());
            
            // Likes y comentarios
            tvLikes.setText(String.valueOf(obs.getNumLikes()));
            tvComentarios.setText(String.valueOf(obs.getNumComentarios()));

            // Imagen (usando Glide para cargar imágenes de internet)
            if (obs.getImagenesUrl() != null && !obs.getImagenesUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(obs.getImagenesUrl().get(0))
                        .centerCrop()
                        .into(ivImagen);
                ivImagen.setVisibility(View.VISIBLE);
            } else {
                ivImagen.setVisibility(View.GONE);
            }
        }

        private int getColorTipo(String tipo) {
            if (tipo == null) return 0xFF888888;
            switch (tipo) {
                case "PLANTA": return 0xFF4CAF50;   // Verde
                case "RINCON": return 0xFF2196F3; // Azul
                case "DENUNCIA": return 0xFFF44336; // Rojo
                default: return 0xFF888888;
            }
        }
    }
}
