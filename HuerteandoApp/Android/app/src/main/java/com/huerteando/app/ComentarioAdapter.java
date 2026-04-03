package com.huerteando.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.clases.Comentario;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter para mostrar la lista de comentarios
 */
public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private List<Comentario> comentarios;

    public ComentarioAdapter(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(comentarios.get(position));
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvAutor;
        private TextView tvContenido;
        private TextView tvFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }

        public void bind(Comentario comentario) {
            tvAutor.setText(comentario.getAutorNick());
            tvContenido.setText(comentario.getContenido());
            tvFecha.setText(comentario.getCreadoEn());

            // Cargar avatar
            if (comentario.getAutorAvatarUrl() != null && !comentario.getAutorAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(comentario.getAutorAvatarUrl())
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }
}
