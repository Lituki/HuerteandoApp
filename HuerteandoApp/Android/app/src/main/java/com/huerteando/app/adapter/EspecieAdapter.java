package com.huerteando.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huerteando.app.R;
import com.huerteando.app.clases.Especie;

import java.util.List;

public class EspecieAdapter extends RecyclerView.Adapter<EspecieAdapter.ViewHolder> {

    private final List<Especie> especies;
    private final OnEspecieActionListener listener;

    public interface OnEspecieActionListener {
        void onEdit(Especie especie);
        void onDelete(Especie especie);
    }

    public EspecieAdapter(List<Especie> especies, OnEspecieActionListener listener) {
        this.especies = especies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_especie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(especies.get(position));
    }

    @Override
    public int getItemCount() {
        return especies.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombreComun, tvNombreCientifico, tvFamilia;
        private final ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreComun = itemView.findViewById(R.id.tvEspecieNombreComun);
            tvNombreCientifico = itemView.findViewById(R.id.tvEspecieNombreCientifico);
            tvFamilia = itemView.findViewById(R.id.tvEspecieFamilia);
            btnEditar = itemView.findViewById(R.id.btnEditarEspecie);
            btnEliminar = itemView.findViewById(R.id.btnEliminarEspecie);
        }

        public void bind(Especie especie) {
            tvNombreComun.setText(especie.getNombreComun());
            tvNombreCientifico.setText(especie.getNombreCientifico());
            tvFamilia.setText(especie.getFamilia());

            btnEditar.setOnClickListener(v -> listener.onEdit(especie));
            btnEliminar.setOnClickListener(v -> listener.onDelete(especie));
        }
    }
}
