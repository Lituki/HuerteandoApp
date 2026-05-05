package com.huerteando.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.clases.Observacion;
import com.huerteando.app.clases.Imagen;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter para el RecyclerView de observaciones.
 * Actualizado para el nuevo modelo del Manual.
 */
public class ObservacionAdapter extends RecyclerView.Adapter<ObservacionAdapter.ViewHolder> {

    private final List<Observacion> observaciones;
    private final OnObservacionClickListener listener;

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

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImagen, ivMeGusta;
        private final TextView tvTitulo, tvTipo, tvZona, tvFecha, tvMeGusta, tvComentarios;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivImagen);
            ivMeGusta = itemView.findViewById(R.id.ivMeGusta);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvZona = itemView.findViewById(R.id.tvZona);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvMeGusta = itemView.findViewById(R.id.tvMeGusta);
            tvComentarios = itemView.findViewById(R.id.tvComentarios);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onObservacionClick(observaciones.get(position));
                }
            });
        }

        public void bind(Observacion obs) {
            tvTitulo.setText(obs.getTitulo());
            
            if (obs.getTipoObservacion() != null) {
                tvTipo.setText(obs.getTipoObservacion().getNombre());
                tvTipo.setBackgroundColor(getColorTipo(obs.getTipoObservacion().getNombre()));
            }

            tvZona.setText(obs.getNombreZona() != null ? obs.getNombreZona() : "Sin zona");
            tvFecha.setText(obs.getFechaObservacion());

            tvMeGusta.setText(String.valueOf(obs.getNumMeGustas()));
            tvComentarios.setText(String.valueOf(obs.getNumComentarios()));

            if (ivMeGusta != null) {
                ivMeGusta.setImageResource(obs.isMeGustaPropio() ? R.drawable.ic_heart_full : R.drawable.ic_heart_empty);
                if (obs.isMeGustaPropio()) {
                    ivMeGusta.setColorFilter(android.graphics.Color.RED);
                } else {
                    ivMeGusta.clearColorFilter();
                }
            }

            // Imagen
            if (obs.getImagenes() != null && !obs.getImagenes().isEmpty()) {
                String url = obs.getImagenes().get(0).getUrlArchivo();
                if (url != null && !url.isEmpty()) {
                    if (!url.startsWith("http")) {
                        String base = ApiClient.BASE_URL;
                        if (base.endsWith("/") && url.startsWith("/")) url = base + url.substring(1);
                        else if (!base.endsWith("/") && !url.startsWith("/")) url = base + "/" + url;
                        else url = base + url;
                    }

                    Glide.with(itemView.getContext())
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .centerCrop()
                            .into(ivImagen);
                    ivImagen.setVisibility(View.VISIBLE);
                }
            } else {
                ivImagen.setVisibility(View.GONE);
            }
        }

        private int getColorTipo(String nombre) {
            if (nombre == null) return 0xFF888888;
            String n = nombre.toUpperCase();
            if (n.contains("PLANTA")) return 0xFF4CAF50;
            if (n.contains("RINCON") || n.contains("RINCÓN")) return 0xFF2196F3;
            if (n.contains("INCIDENCIA") || n.contains("DENUNCIA")) return 0xFFF44336;
            return 0xFF888888;
        }
    }
}