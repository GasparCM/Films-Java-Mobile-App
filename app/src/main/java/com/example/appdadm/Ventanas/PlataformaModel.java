package com.example.appdadm.Ventanas;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdadm.R;
import com.example.appdadm.adaptadores.AdapterRecyclerViewPeliculas;
import com.example.appdadm.objetos.Pelicula;
import com.example.appdadm.objetos.Plataforma;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlataformaModel extends AppCompatActivity {

    //variable para controlar en el onactivityresult si viene de editar plataforma o añadir
    private int opcionImagen;
    private String urlCaratulaPeliAux;
    private String urlAux;
    private String urlImagePlatafAux;
    private static final int REQUEST_STORAGE_PERMISSION = 123; // Puedes elegir cualquier número que desees aquí
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_SELECT_IMAGE_FROM_GALLERY = 2;
    private DataBaseHelper db;
    private ArrayList<Pelicula> arrayPeliculas;
    private AdapterRecyclerViewPeliculas listAdapter;
    private RecyclerView recycView;
    private Plataforma platafAux;
    private Usuario usuAux;
    private Pelicula peliculaAux;
    private TextView nombrePlataforma;
    private TextView urlPlataforma;
    private EditText tituloPeli;
    private EditText duracionPeli;
    private Spinner generoPeli;
    private NumberPicker valoracionPeli;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_plataforma);
        db = new DataBaseHelper(this);

        Intent intent = getIntent();

        // Verificar si el Intent contiene datos extras
        if (intent != null && intent.hasExtra("plataforma") && intent.hasExtra("usuario")) {
            platafAux = (Plataforma) intent.getSerializableExtra("plataforma");
            usuAux = (Usuario) intent.getSerializableExtra("usuario");

            setContent();//llenamos los campos imagen titulo i url

            arrayPeliculas = db.getAllPeliculasFromUserPlatform(this.usuAux.getEmail(),String.valueOf(this.platafAux.getId()));

            if (arrayPeliculas.isEmpty()) {
                Toast.makeText(this, "No hay peliculas disponibles en esta plataforma", Toast.LENGTH_SHORT).show();
            } else {
                mostrarRecyclerPeliculas();
            }
        }
        else{
            finish();
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refreshRecyclerViewPeliculas();
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
    private void setContent(){
        mostrarImagenDesdeRuta(this.platafAux.getImageUrl());
        nombrePlataforma = findViewById(R.id.txtVTituloPlataforma);
        urlPlataforma = findViewById(R.id.txtVUrlPlataforma);
        nombrePlataforma.setText(platafAux.getNombre());
        urlPlataforma.setText(platafAux.getUrl());
        mostrarRecyclerPeliculas();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("entraActivityResult", "entraActivity");
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            Uri imageUri = saveBitmapToStorage(imageBitmap);
                            if (imageUri != null) {

                                //si es 2 viene de añadir pelicula
                                if (this.opcionImagen == 2){
                                    this.urlCaratulaPeliAux = imageUri.toString();
                                    mostrarImagenPeliculaDesdeRuta(this.urlCaratulaPeliAux);
                                    Log.d("URL ImagenPelicula", urlCaratulaPeliAux); // Imprimir la URL de la imagen
                                }
                                else{
                                    //si es 1 viene de editar plataforma
                                    if (this.opcionImagen == 1){
                                        this.urlImagePlatafAux = imageUri.toString();
                                        mostrarImagenEditarPlataforma(this.urlImagePlatafAux);
                                        Log.d("URL ImagenPlataforma", urlImagePlatafAux); // Imprimir la URL de la imagen
                                    }
                                }
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

                        if (this.opcionImagen == 2){
                            this.urlCaratulaPeliAux = selectedImageUri.toString();
                            mostrarImagenPeliculaDesdeRuta(this.urlCaratulaPeliAux);
                        }
                        else {
                            //si es 1 viene de editar plataforma
                            if (this.opcionImagen == 1) {
                                this.urlImagePlatafAux = selectedImageUri.toString();
                                mostrarImagenEditarPlataforma(this.urlCaratulaPeliAux);
                            }
                        }
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

    private void mostrarRecyclerPeliculas(){
        arrayPeliculas = db.getAllPeliculasFromUserPlatform(this.usuAux.getEmail(),String.valueOf(this.platafAux.getId()));
        listAdapter = new AdapterRecyclerViewPeliculas(arrayPeliculas, this,this.usuAux);
        recycView = findViewById(R.id.recyclerViewPeliculas);
        recycView.setHasFixedSize(true);
        recycView.setLayoutManager(new GridLayoutManager(this, 2));
        recycView.setAdapter(listAdapter);
    }

    private void refreshRecyclerViewPeliculas() {
        arrayPeliculas.clear();
        arrayPeliculas.addAll(db.getAllPeliculasFromUser(this.usuAux.getEmail()));
        if (listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
        else{
            mostrarRecyclerPeliculas();
        }
    }

    public void guardarPelicula(View v) {
        try {
            if (TextUtils.isEmpty(this.duracionPeli.getText().toString())) {
                duracionPeli.setError("Este campo no puede estar vacío");
            }
            else if (TextUtils.isEmpty(this.tituloPeli.getText().toString())) {
                tituloPeli.setError("Este campo no puede estar vacío");
            }
            else {
                String gen = generoPeli.getSelectedItem().toString();
                peliculaAux = new Pelicula(this.platafAux.getId(), this.usuAux.getEmail(), this.valoracionPeli.getValue(), this.duracionPeli.getText().toString(), this.tituloPeli.getText().toString(), gen,this.urlCaratulaPeliAux);
                long i = db.insertarPelicula(peliculaAux);
                if (i == -1) {
                    Toast.makeText(this, "ERROR INSERT PELI", Toast.LENGTH_SHORT).show();
                }
                else {
                    setContentView(R.layout.activity_info_plataforma);
                    setContent();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abrirPeliculas(View v){
        this.opcionImagen = 2;
        setContentView(R.layout.activity_anyadir_peliculas);
        valoracionPeli = findViewById(R.id.numbValoracionPeli);
        generoPeli = findViewById(R.id.spinnerGeneroPeli);
        tituloPeli = findViewById(R.id.etxtNombrePeli);
        duracionPeli = findViewById(R.id.etxtMinutosPeli);
        valoracionPeli.setMaxValue(5);
        valoracionPeli.setMinValue(0);
    }
    public void volverAtras(View v){
        Intent resultIntent = new Intent();
        // Pon los datos actualizados en el intent, si es necesario
        setResult(RESULT_OK, resultIntent);
        finish();
    }


    public void volverAtrasEditarPlataf(View v){
        setContentView(R.layout.activity_info_plataforma);
        setContent();
        this.opcionImagen=2;
    }

    public void editarPlataforma(View v){
        setContentView(R.layout.activity_editar_plataforma);
        this.opcionImagen = 1;
    }

    public void guardarPlatformaEditada(View v){

        EditText nuevoNombre = findViewById(R.id.etxtNombreNuevoPlataf);
        EditText nuevaUrl = findViewById(R.id.etxtUrlNuevoPlataf);
        EditText nuevaContra = findViewById(R.id.eTxtPasswordNuevoPlataformas);

        if (TextUtils.isEmpty(nuevoNombre.getText().toString())){
            nuevoNombre.setError("Introduce el nuevo nombre");
        }
        else if (TextUtils.isEmpty(nuevaUrl.getText().toString())){
            nuevaUrl.setError("Introduce la nueva url");
        }
        else if (TextUtils.isEmpty(nuevaContra.getText().toString())){
            nuevaContra.setError("Introduce la nueva contra");
        }
        else{
            Plataforma nuevaPlataforma = new Plataforma();
            nuevaPlataforma.setNombre(nuevoNombre.getText().toString());
            nuevaPlataforma.setUrl(nuevaUrl.getText().toString());
            nuevaPlataforma.setPassword(nuevaContra.getText().toString());

            if (this.urlImagePlatafAux == null || this.urlImagePlatafAux.isEmpty()){
                nuevaPlataforma.setImageUrl(this.platafAux.getImageUrl());
            }
            else{
                nuevaPlataforma.setImageUrl(urlImagePlatafAux);
            }

            nuevaPlataforma.setId(this.platafAux.getId());
            nuevaPlataforma.setIdUsuario(this.platafAux.getIdUsuario());
            this.db.updatePlataforma(nuevaPlataforma);
            setContentView(R.layout.activity_info_plataforma);
            this.platafAux = nuevaPlataforma;
            setContent();
        }
    }

    private void mostrarImagenDesdeRuta(String imagePath) {

        ImageView imageView = findViewById(R.id.imgVPlataforma);

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

    private void mostrarImagenEditarPlataforma(String imagePath){

        ImageView imageView = findViewById(R.id.imgVEditarPlataf);

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

            Toast.makeText(this, "Error al mostrar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    private void mostrarImagenPeliculaDesdeRuta(String imagePath) {

        ImageView imageView = findViewById(R.id.imgVPelic);

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
                Toast.makeText(this, "Error, vuelve a intentarlo", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){

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
