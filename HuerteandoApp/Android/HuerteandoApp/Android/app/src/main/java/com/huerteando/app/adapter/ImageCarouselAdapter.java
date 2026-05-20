package com.huerteando.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huerteando.app.R;
import com.huerteando.app.api.ApiClient;
import com.huerteando.app.clases.Imagen;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {

    private final List<Imagen> imagenes;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String url);
    }

    public ImageCarouselAdapter(List<Imagen> imagenes, OnImageClickListener listener) {
        this.imagenes = imagenes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carousel_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(imagenes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivCarouselImage);
        }

        public void bind(Imagen img, OnImageClickListener listener) {
            String url = img.getUrlArchivo();
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http")) {
                    String base = ApiClient.BASE_URL;
                    if (base.endsWith("/") && url.startsWith("/")) url = base + url.substring(1);
                    else if (!base.endsWith("/") && !url.startsWith("/")) url = base + "/" + url;
                    else url = base + url;
                }

                // Log para depuración
                android.util.Log.d("ImageCarouselAdapter", "Cargando carrusel: " + url);

                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(imageView);

                final String finalUrl = url;
                imageView.setOnClickListener(v -> {
                    if (listener != null) listener.onImageClick(finalUrl);
                });
            }
        }
    }
}