package com.huerteando.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.R;
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
        holder.bind(observaciones.get(position));
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
        private final TextView tvTitulo, tvTipo, tvZona, tvFecha, tvLikes, tvComentarios;

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
            tvTipo.setBackgroundColor(getColorTipo(obs.getTipoObservacion()));
            tvZona.setText(obs.getNombreZona() != null ? obs.getNombreZona() : "Sin zona");
            tvFecha.setText(obs.getFechaObservacion());
            tvLikes.setText(String.valueOf(obs.getNumLikes()));
            tvComentarios.setText(String.valueOf(obs.getNumComentarios()));

            if (obs.getImagenesUrl() != null && !obs.getImagenesUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(obs.getImagenesUrl().get(0)).centerCrop().into(ivImagen);
                ivImagen.setVisibility(View.VISIBLE);
            } else {
                ivImagen.setVisibility(View.GONE);
            }
        }

        private int getColorTipo(String tipo) {
            if (tipo == null) return 0xFF888888;
            switch (tipo) {
                case "PLANTA": return 0xFF4CAF50;
                case "RINCON": return 0xFF2196F3;
                case "INCIDENCIA": return 0xFFF44336;
                default: return 0xFF888888;
            }
        }
    }
}
