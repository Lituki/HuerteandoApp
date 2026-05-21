package com.huerteando.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 1280;
    private static final long MAX_SIZE_BYTES = 1000000; // 1MB aprox

    /**
     * Comprime y redimensiona una imagen desde una Uri a un archivo temporal.
     * @param context Contexto de la aplicación
     * @param uri Uri de la imagen original
     * @param prefix Prefijo para el nombre del archivo (ej: "obs" o "avatar")
     * @param index Índice para evitar colisiones de nombres en procesos rápidos
     * @return El archivo comprimido o null si falla
     */
    public static File compressImage(Context context, Uri uri, String prefix, int index) {
        try {
            // 1. Decodificar dimensiones para redimensionar (In-memory reduction)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(is, null, options);
            }

            int width = options.outWidth;
            int height = options.outHeight;
            Log.d(TAG, "Dimensiones originales: " + width + "x" + height);

            int inSampleSize = 1;
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= MAX_HEIGHT && (halfWidth / inSampleSize) >= MAX_WIDTH) {
                    inSampleSize *= 2;
                }
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            // 2. Decodificar el bitmap con el sample size
            Bitmap bitmap;
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (bitmap == null) return null;

            // 3. Compresión iterativa de calidad
            File outFile = new File(context.getCacheDir(), prefix + "_" + System.currentTimeMillis() + "_" + index + ".jpg");
            int quality = 90;
            long fileSize;
            
            do {
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                    fos.flush();
                }
                fileSize = outFile.length();
                Log.d(TAG, "Calidad: " + quality + " - Tamaño: " + fileSize + " bytes");
                quality -= 10;
            } while (fileSize > MAX_SIZE_BYTES && quality > 10);

            bitmap.recycle();
            return outFile;

        } catch (IOException e) {
            Log.e(TAG, "Error al comprimir imagen", e);
            return null;
        }
    }
}
