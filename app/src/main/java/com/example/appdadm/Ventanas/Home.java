package com.example.appdadm.Ventanas;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.appdadm.adaptadores.AdapterRecyclerViewPlataformas;
import com.example.appdadm.objetos.Pelicula;
import com.example.appdadm.objetos.Plataforma;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Home extends AppCompatActivity implements AdapterRecyclerViewPlataformas.OnPlataformaDeletedListener{
    private static final int REQUEST_CODE_ACTUALIZACION = 13;
    private static final int REQUEST_STORAGE_PERMISSION = 12345;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_SELECT_IMAGE_FROM_GALLERY = 2;
    private Usuario usuAux;
    private String imagePath;
    private Plataforma plataformaAux;
    private DataBaseHelper db;
    private ArrayList<Plataforma> arrayPlataformas;
    private ArrayList<Pelicula> arrayPeliculas;
    private AdapterRecyclerViewPlataformas listAdapterPlataformas;
    private AdapterRecyclerViewPeliculas listAdapterPeliculas;
    private RecyclerView recycView;
    private EditText urlPlataforma;
    private EditText nomPlataforma;
    private EditText psswdPlataforma;
    private ImageView imgVPlataf;

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.usuAux = (Usuario) intent.getSerializableExtra("usuario");
        refreshRecyclerViewPlataformas();
        refreshRecyclerViewPeliculas();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = new DataBaseHelper(this);

        Intent intent = getIntent();

        // Verificar si el Intent contiene datos extras
        if (intent != null && intent.hasExtra("USUARIO_AUX")) {
            // Obtener el objeto MiObjeto de los extras
            usuAux = (Usuario) intent.getSerializableExtra("USUARIO_AUX");
        }

        arrayPlataformas = db.getAllPlataformas(this.usuAux);

        if (arrayPlataformas.isEmpty()) {
            Toast.makeText(this, "No hay plataformas disponibles", Toast.LENGTH_SHORT).show();
        } else {
            mostrarRecyclerPlataformas();

            arrayPeliculas = db.getAllPeliculasFromUser(this.usuAux.getEmail());

            if (arrayPeliculas.isEmpty()) {
                Toast.makeText(this, "No hay peliculas disponibles", Toast.LENGTH_SHORT).show();
            } else {
                mostrarRecyclerPeliculas();
            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("entraActivityResult", "entraActivity");
        try {
            if (requestCode == REQUEST_CODE_ACTUALIZACION && resultCode == RESULT_OK) {
                refreshRecyclerViewPlataformas();
                refreshRecyclerViewPeliculas();
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            Uri imageUri = saveBitmapToStorage(imageBitmap);
                            if (imageUri != null) {
                                mostrarImagenDesdeRuta(imageUri.toString());
                                this.imagePath = imageUri.toString();
                            } else {
                                Log.e("Home", "Failed to save image to storage");
                            }
                        } else {
                            Log.e("Home", "Failed to retrieve captured image");
                        }
                    } else {
                        Log.e("Home", "Null extras in returned data");
                    }
                } else {
                    Log.e("Home", "Null data returned after capturing image");
                }
            }
            else if (requestCode == REQUEST_SELECT_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        this.imagePath = selectedImageUri.toString();
                        try {
                            mostrarImagenDesdeRuta(this.imagePath);
                        } catch (Exception e) {
                            Log.e("Home", "Error displaying selected image: " + e.getMessage());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            }
        }
    }
    private void mostrarImagenDesdeRuta(String imagePat) {
        ImageView imageView = findViewById(R.id.imgVPlataf);

        try {
            if (imagePat != null) {
                Glide.with(this)
                        .load(imagePat)
                        .into(imageView);
            } else {
                Toast.makeText(this, "Error, vuelve a intentarlo", Toast.LENGTH_SHORT).show();
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
    private void mostrarRecyclerPlataformas(){
        arrayPlataformas = db.getAllPlataformas(this.usuAux);
        listAdapterPlataformas = new AdapterRecyclerViewPlataformas(arrayPlataformas, this.usuAux, this);
        // Asignar el listener al RecyclerView de plataformas
        listAdapterPlataformas.setOnPlataformaDeletedListener(new AdapterRecyclerViewPlataformas.OnPlataformaDeletedListener() {
            @Override
            public void onPlataformaDeleted(Plataforma plataforma) {
                // Actualizar el RecyclerView de películas cuando se elimine una plataforma
                refreshRecyclerViewPlataformas();
                refreshRecyclerViewPeliculas();

            }
        });
        recycView = findViewById(R.id.RecyclerViewPlataf);
        recycView.setHasFixedSize(true);
        recycView.setLayoutManager(new GridLayoutManager(this, 1));
        recycView.setAdapter(listAdapterPlataformas);
    }

    private void mostrarRecyclerPeliculas(){
        arrayPeliculas = db.getAllPeliculasFromUser(this.usuAux.getEmail());
        listAdapterPeliculas = new AdapterRecyclerViewPeliculas(arrayPeliculas, this, this.usuAux);
        recycView = findViewById(R.id.RecyclerViewPelis);
        recycView.setHasFixedSize(true);
        recycView.setLayoutManager(new GridLayoutManager(this, 2));
        recycView.setAdapter(listAdapterPeliculas);
    }

    private void refreshRecyclerViewPlataformas() {
        arrayPlataformas.clear();
        arrayPlataformas.addAll(db.getAllPlataformas(this.usuAux));
        if (listAdapterPlataformas != null){
            listAdapterPlataformas.notifyDataSetChanged();
        }
        else{
            mostrarRecyclerPlataformas();
        }
    }

    private void refreshRecyclerViewPeliculas() {
        arrayPeliculas.clear();
        arrayPeliculas.addAll(db.getAllPeliculasFromUser(this.usuAux.getEmail()));
        if (listAdapterPeliculas != null){
            listAdapterPeliculas.notifyDataSetChanged();
        }
        else{
            mostrarRecyclerPeliculas();
        }
    }

    public void mostrarDialogoSeleccion(View v) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogCustomStyle);
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

    public void solicitarPermisoYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
        }
        else{
            abrirCamara();
        }
    }

    public void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_FROM_GALLERY);
    }
    public void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //HAY QUE HACERLO DE ESTA MANERA PORQUE WRITE_EXTERNAL_STORAGE ESTA DEPRECATED PARA SDK MAYORES DE 30 Y NO FUNCIONA DE LA FORMA TRADICIONAL
    public void solicitarPermisoYExportarBaseDeDatos(View v) {

        //Permisos para las versionde de sdk de 23 a la 29
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
            else{
                exportWithPermission();
            }
        }

        //Permisos para las versionde de sdk 30 o superiores
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            if (!Environment.isExternalStorageManager()){
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent,101);
                }
                catch (Exception e){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent,101);
                }
            }
            else{
                exportWithPermission();
            }
        }
    }
    private void exportTable(String tableName, SQLiteDatabase db, FileWriter writer) throws IOException {

        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        int columnCount = cursor.getColumnCount();

        // Escribir los nombres de las columnas en la primera línea
        for (int i = 0; i < columnCount; i++) {
            writer.append(cursor.getColumnName(i));
            if (i < columnCount - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");

        // Escribir los datos de la tabla
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnCount; i++) {
                writer.append(cursor.getString(i));
                if (i < columnCount - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
        }

        cursor.close();
    }

    // Método para exportar la base de datos una vez que se tenga el permiso
    private void exportWithPermission() {
        // Obtener la ruta de la carpeta de documentos
        String documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        String filePath = documentsDirectory + File.separator + "database_export.txt";

        try (FileWriter writer = new FileWriter(filePath)) {
            SQLiteDatabase db = this.db.getReadableDatabase();
            boolean anyDataExported = false;

            if (tableHasData("USUARIO", db, writer)) {
                anyDataExported = true;
            }
            if (tableHasData("PLATAFORMAS", db, writer)) {
                anyDataExported = true;
            }

            db.close();

            if (anyDataExported) {
                Toast.makeText(this, "Exportada Correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "La base de datos está vacía", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar la base", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean tableHasData(String tableName, SQLiteDatabase db, FileWriter writer) throws IOException {
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        int rowCount = cursor.getCount();
        cursor.close();

        if (rowCount > 0) {
            exportTable(tableName, db, writer);
            return true;
        } else {
            return false;
        }
    }

    public void guardarPlataforma(View v){

        if (TextUtils.isEmpty(this.urlPlataforma.getText().toString())){
            urlPlataforma.setError("Este campo no puede estar vacio");
        }
        else if (TextUtils.isEmpty(this.nomPlataforma.getText().toString())){
            nomPlataforma.setError("Este campo no puede estar vacio");
        }
        else if (TextUtils.isEmpty(this.psswdPlataforma.getText().toString())){
            psswdPlataforma.setError("Este campo no puede estar vacio");
        }
        else {
            plataformaAux = new Plataforma(this.usuAux.getEmail(),this.nomPlataforma.getText().toString(), this.urlPlataforma.getText().toString() , this.psswdPlataforma.getText().toString(),this.imagePath);

            //si va muy mal plantearlo sin pasar el bytearray a otra funcion sino usarlo aqui directamente en una en esta clase
            long i = this.db.insertarPlataforma(plataformaAux);
            if (i == -1){
                Toast.makeText(this, "Error al guardar la plataforma", Toast.LENGTH_SHORT).show();
            }
            else{
                setContentView(R.layout.activity_home);
                mostrarRecyclerPlataformas();
                mostrarRecyclerPeliculas();
                Toast.makeText(this, "Plataforma Guardada", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void abrirAnyadir(View v){
        setContentView(R.layout.activity_anyadir_contenido);
    }

    public void abrirHome(View v){
        setContentView(R.layout.activity_home);
        mostrarRecyclerPeliculas();
        mostrarRecyclerPlataformas();
    }

    public void abrirPlataformas(View v){
        setContentView(R.layout.activity_anyadir_plataformas);
        nomPlataforma = findViewById(R.id.etxtNombrePlataf);
        urlPlataforma = findViewById(R.id.etxtUrlPlataf);
        psswdPlataforma = findViewById(R.id.eTxtPasswordPlataformas);
        imgVPlataf = findViewById(R.id.imgVPlataf);
    }

    public void abrirPerfil(View v){
        setContentView(R.layout.activity_perfil);
        TextView txtNombre = findViewById(R.id.txtVNombreUsuario);
        TextView txtEdad = findViewById(R.id.txtVEdad);
        ImageView imgPerfil = findViewById(R.id.imageView);
        txtNombre.setText(usuAux.getNombre() + " " + usuAux.getApellidos());
        txtEdad.setText(usuAux.getIntereses());
    }

    public void cerrarSesion(View v){
        finish();
    }
    // Método para cargar una imagen desde una ruta y mostrarla en un ImageView


    //este no hace nada el que hace es el de arriba que tiene el listener pero tengo  que hacerlo par aplicar la interfaz
    @Override
    public void onPlataformaDeleted(Plataforma plataforma) {
        refreshRecyclerViewPlataformas();
        refreshRecyclerViewPeliculas();
    }
}
