package com.example.appdadm.Ventanas;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.appdadm.R;
import com.example.appdadm.objetos.Pelicula;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PeliculaModel extends AppCompatActivity {
    private Pelicula peliAux;
    private String urlCaratulaAux;
    private Usuario usuAux;
    private ImageView imgPeli;
    private TextView txtVTitutlo;
    private TextView txtVGenero;
    private TextView txtVCalificacion;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_SELECT_IMAGE_FROM_GALLERY = 2;
    private static final int REQUEST_ACTUALIZACION_PELI = 3;

    private DataBaseHelper db = new DataBaseHelper(this);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_pelicula);
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("pelicula") && intent.hasExtra("usuario")) {
            peliAux = (Pelicula) intent.getSerializableExtra("pelicula");
            usuAux = (Usuario) intent.getSerializableExtra("usuario");
            setInfo();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ENTRARESULT", "ENTRARESULT");
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            Uri imageUri = saveBitmapToStorage(imageBitmap);
                            if (imageUri != null) {
                                this.urlCaratulaAux = imageUri.toString();
                                mostrarImagenDesdeRuta(this.urlCaratulaAux);
                            } else {
                                Log.e("Home", "Failed to save image to storage");
                            }
                        } else {
                            Toast.makeText(this, "Error: No se pudo obtener la imagen capturada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: No se recibieron extras de la cámara", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error: No se recibieron datos de la cámara", Toast.LENGTH_SHORT).show();
                }
            }else if (requestCode == REQUEST_SELECT_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        this.urlCaratulaAux = selectedImageUri.toString();
                        mostrarImagenDesdeRuta(this.urlCaratulaAux);
                    } else {
                        Log.e("Home", "Selected image URI is null");
                    }
                } else {
                    Log.e("Home", "Null data returned after selecting image from gallery");
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    public void mostrarDialogoSeleccion(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustomStyle);
        builder.setTitle("Seleccionar fuente de imagen")
                .setMessage("¿Desde dónde desea cargar la imagen?")
                .setPositiveButton("Cámara", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        solicitarPermisoYAbrirCamara();
                    }
                })
                .setNegativeButton("Galería", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        abrirGaleria();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_FROM_GALLERY);
    }
    public void solicitarPermisoYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Verificar compatibilidad antes de abrir la cámara
            if (verificarCompatibilidadCamara()) {
                abrirCamara();
            } else {
                Toast.makeText(this, "No se encontró ninguna aplicación de cámara instalada", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean verificarCompatibilidadCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent.resolveActivity(getPackageManager()) != null;
    }
    public void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // Cambiado de REQUEST_CAMERA_PERMISSION a REQUEST_IMAGE_CAPTURE
        }
    }
    private void setInfo(){
        try {
            this.imgPeli = findViewById(R.id.imgPelicula);
            this.txtVGenero = findViewById(R.id.txtVGenero);
            this.txtVTitutlo = findViewById(R.id.txtVTitulo);
            this.txtVCalificacion = findViewById(R.id.txtVCalificacion);

            if (this.peliAux.getCaratuaUrl() != null){
                Glide.with(this)
                        .load(this.peliAux.getCaratuaUrl())
                        .into(imgPeli);
            }
            else{
                Toast.makeText(this, "Error, vuelve a intentarlo", Toast.LENGTH_SHORT).show();
            }
            txtVTitutlo.setText(peliAux.getTitulo());
            txtVGenero.setText(peliAux.getGenero());
            txtVCalificacion.setText(String.valueOf(peliAux.getCalificacion()));
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error en la imagen,vuelve a intentarlo", Toast.LENGTH_SHORT).show();
        }
    }

    public void volverAtras(View v){
        Intent resultIntent = new Intent();
        // Pon los datos actualizados en el intent, si es necesario
        setResult(REQUEST_ACTUALIZACION_PELI, resultIntent);
        finish();
    }

    public void volverAtrasEditarPeli(View v)
    {
        setContentView(R.layout.activity_info_pelicula);
        setInfo();
    }

    public void guardarNuevaPeli(View v) {

        EditText nuevoTitulo = findViewById(R.id.etxtNuevoNombrePeli);
        EditText nuevaDuracion = findViewById(R.id.etxtMinutosNuevosPeli);
        Spinner nuevoGenero = findViewById(R.id.spinnerGeneroNuevoPeli);
        NumberPicker nuevaValoracion = findViewById(R.id.numbValoracionNuevoPeli);

        if (TextUtils.isEmpty(nuevoTitulo.getText().toString())){
            nuevoTitulo.setError("Introduce el nuevo nombre");
        }
        else if (TextUtils.isEmpty(nuevaDuracion.getText().toString())){
            nuevaDuracion.setError("Introduce la nueva url");
        }
        else{
            Pelicula nuevaPeli = new Pelicula();
            nuevaPeli.setGenero(nuevoGenero.getSelectedItem().toString());
            nuevaPeli.setTitulo(nuevoTitulo.getText().toString());
            nuevaPeli.setCalificacion(nuevaValoracion.getValue());
            nuevaPeli.setDuracionMinutos(nuevaDuracion.getText().toString());
            nuevaPeli.setId(this.peliAux.getId());
            nuevaPeli.setIdPlataforma(this.peliAux.getIdPlataforma());

            if (this.urlCaratulaAux == null || this.urlCaratulaAux.isEmpty()){
                nuevaPeli.setCaratuaUrl(this.peliAux.getCaratuaUrl());
            }
            else{
                nuevaPeli.setCaratuaUrl(this.urlCaratulaAux);
            }
            db.updatePelicula(nuevaPeli);
            this.peliAux = nuevaPeli;
            setContentView(R.layout.activity_info_pelicula);
            setInfo();
            Log.e("ACABA", "ACABA EDITAR PELI");
        }
    }
        public void editarPelicula(View v){
        Log.e("ACABA", "ENTRA EDITAR PELI");
        setContentView(R.layout.activity_editar_pelicula);
    }
    private void mostrarImagenDesdeRuta(String imagePath) {

        ImageView imageView = findViewById(R.id.imgVPelicNueva);

        try {
            if (imageView != null) {
                if (imagePath != null) {
                    Glide.with(this)
                            .load(imagePath)
                            .into(imageView);
                } else {
                    // Manejar la situación cuando imagePath es nulo
                    Log.e("Home", "Image path is null");
                }
            } else {
                // Manejar la situación cuando imageView es nulo
                Log.e("Home", "ImageView is null");
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error al mostrar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri saveBitmapToStorage(Bitmap bitmap) {
        Context context = getApplicationContext();

        // Directorio de almacenamiento externo donde se guardará la imagen
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_images");
        if (!directory.exists()) {
            directory.mkdirs(); // Crear directorio si no existe
        }

        // Nombre único para el archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";

        // Archivo donde se guardará la imagen
        File imageFile = new File(directory, fileName);

        try {
            // Guardar el bitmap en el archivo de imagen
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Obtener la URI del archivo de imagen utilizando FileProvider
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
