package com.huerteando.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.R;
import com.huerteando.app.clases.Comentario;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter para mostrar la lista de comentarios
 */
public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private final List<Comentario> comentarios;
    private final OnComentarioActionListener listener;
    private final Long currentUserId;

    public interface OnComentarioActionListener {
        void onDelete(Comentario comentario);
    }

    public ComentarioAdapter(List<Comentario> comentarios, Long currentUserId, OnComentarioActionListener listener) {
        this.comentarios = comentarios;
        this.currentUserId = currentUserId;
        this.listener = listener;
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvAutor;
        private final TextView tvContenido;
        private final TextView tvFecha;
        private final View btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEliminar = itemView.findViewById(R.id.btnEliminarComentario);
        }

        public void bind(Comentario comentario) {
            tvAutor.setText(comentario.getAutorNick());
            tvContenido.setText(comentario.getContenido());
            tvFecha.setText(comentario.getCreadoEn());

            // Solo mostrar botón eliminar si el comentario es del usuario actual
            if (currentUserId != null && currentUserId.equals(comentario.getUsuarioId())) {
                btnEliminar.setVisibility(View.VISIBLE);
                btnEliminar.setOnClickListener(v -> listener.onDelete(comentario));
            } else {
                btnEliminar.setVisibility(View.GONE);
            }

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
