package com.example.appdadm.Ventanas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdadm.R;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

public class Registro extends AppCompatActivity {

    Usuario usuAux;
    DataBaseHelper dbHelper;
    EditText email;
    EditText passw;
    EditText passw2;
    EditText nombre;
    EditText apellidos;
    EditText fechaNac;

    EditText respuestaSeg;
    Spinner spinnerSeguridad;
    Switch dobleFactor;
    Spinner spinnerIntereses;
    Button btnRegistrar;
    String contentEmail;
    String contentApellidos;
    String contentNombre;
    String contentPassword;
    String contentPassword2;
    String contentPreguntaSeg;
    String contentRespuesSeg;

    String contentInteres;
    int contentFactor;

    TextView txtLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DataBaseHelper(this);
        
        txtLogin = findViewById(R.id.txtVLogin);
        email = findViewById(R.id.eTEmail);
        passw = findViewById(R.id.editTextPassword);
        passw2 =  findViewById(R.id.etRepetirContra);
        nombre = findViewById(R.id.eTNombre);
        apellidos = findViewById(R.id.eTApellidos);
        fechaNac = findViewById(R.id.eTFecha);
        spinnerSeguridad = findViewById(R.id.spinnerPreguntaSeg);
        respuestaSeg = findViewById(R.id.eTRespuesta);
        dobleFactor = findViewById(R.id.switchFactor);
        spinnerIntereses = findViewById(R.id.spinnerIntereses);
        btnRegistrar = findViewById(R.id.btnRegistrarse);


        // Asignar un click listener al TextView
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar la lógica que se ejecutará cuando el TextView sea clicado
                // Por ejemplo, abrir una nueva actividad o una página web de registro
                //Toast.makeText(LogIn.this, "Botón clicado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar la lógica que se ejecutará cuando el TextView sea clicado
                // Por ejemplo, abrir una nueva actividad o una página web de registro
                //Toast.makeText(LogIn.this, "Botón clicado", Toast.LENGTH_SHORT).show();
                comprobarCampos();
            }
        });
    }
    private boolean comprobarCampos(){
        //Campos oblgatorios
        contentEmail = email.getText().toString();
        contentNombre = nombre.getText().toString();
        contentPassword = passw.getText().toString();
        contentPassword2 = passw2.getText().toString();

        //Campos no Obligatorios
        contentApellidos = apellidos.getText().toString();
        contentRespuesSeg = respuestaSeg.getText().toString();

        //Cojemos el valor de los spinners como Strings
        Object selectedSeguridad = spinnerSeguridad.getSelectedItem();
        contentPreguntaSeg = (selectedSeguridad != null) ? selectedSeguridad.toString() : null;

        Object selectedInteres = spinnerIntereses.getSelectedItem();
        contentInteres = (selectedInteres != null) ? selectedInteres.toString() : null;
        //

        contentFactor = asignarDobleFact();

        if(TextUtils.isEmpty(contentNombre)){
            nombre.setError("Este campo no puede estar vacío");
            return false;
        }
        if (TextUtils.isEmpty(contentEmail)) {
            // El EditText está vacío
            email.setError("Este campo no puede estar vacío");
            return false;
        }
        if(TextUtils.isEmpty(contentPassword)){
            passw.setError("Este campo no puede estar vacío");
            return false;
        }
        if(!esEmailValido(contentEmail)){
            email.setError("El formato no es correcto");
            return false;
        }
        if(!TextUtils.equals(contentPassword,contentPassword2)){
            passw2.setError("Las Contraseñas No coinciden");
            return false;
        }
        if (usuarioExiste(dbHelper, contentEmail)) {
            email.setError("Este email ya esta en uso");
            return false;
        }
        else{
            usuAux = new Usuario(contentNombre,contentApellidos,contentEmail,contentPreguntaSeg,contentRespuesSeg,contentPassword,contentFactor);
            usuAux.setIntereses(contentInteres);
            //faltaria meter los demas atributos al usuario no obligatorios

            registrarUsuario(dbHelper,usuAux);//registramos al usuario en la base de datos

            Intent intent = new Intent(Registro.this,Home.class);
            intent.putExtra("USUARIO_AUX",usuAux);//pasamos el objeto usuario a la actividad home
            startActivity(intent);
            vaciarCampos();
            return true;
        }
    }

    private void registrarUsuario(DataBaseHelper dbHelper, Usuario usu) {

        // Comprobar si el usuario ya existe en la base de datos
        if (usuarioExiste(dbHelper, usu.getEmail())) {
            // El usuario ya existe, muestra un mensaje o realiza alguna acción
            Toast.makeText(Registro.this, "El email ya está registrado", Toast.LENGTH_SHORT).show();
        } else {
            // El usuario no existe, procede con la inserción
            long newRowId = dbHelper.insertarUsuario(usu.getEmail(), usu.getNombre(), usu.getApellidos(), "2024-02-25", usu.getPreguntaSeg(), usu.getRespuestaSeg(), usu.getIntereses(), usu.getDobleFactor(), usu.getPassword());

            if (newRowId != -1) {
                // Éxito al insertar
                Toast.makeText(Registro.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                // Puedes hacer otras acciones después de un registro exitoso
            } else {
                // Error al insertar
                Toast.makeText(Registro.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Metodo que comprueba si ya existe un usuario con ese email
     * @param dbHelper
     * @param email
     * @return
     */
    private boolean usuarioExiste(DataBaseHelper dbHelper, String email) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean existe = false;

        try {
            // Abrir la base de datos en modo lectura
            db = dbHelper.getReadableDatabase();

            // Consulta para verificar si el usuario existe
            String consulta = "SELECT * FROM USUARIO WHERE email = ?";
            String[] args = {email};

            // Ejecutar la consulta
            cursor = db.rawQuery(consulta, args);

            // Verificar si el usuario existe
            existe = cursor.getCount() > 0;

        } catch (Exception e) {
            // Manejar la excepción aquí (puedes imprimir un mensaje de error, registrar en un archivo de registro, etc.)
            e.printStackTrace();
        } finally {
            // Cerrar el cursor y la base de datos en el bloque finally para asegurar que se cierren incluso si hay una excepción
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
        return existe;
    }

    /**
     * Metodo para compronbar que el email es valido
     * @param email
     * @return
     */
    private boolean esEmailValido(String email) {
        // Utiliza el método matcher de la clase Patterns para comparar con el patrón de correo electrónico
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void mostrarCalendario(View v){

        DatePickerDialog d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anyo, int mes, int dia) {
                fechaNac.setText(dia + "/" + (mes+1) + "/" + anyo);
            }
        },2024,3,27);
        d.show();
    }

    private int asignarDobleFact(){
        if (dobleFactor.isChecked()==true){
            return 1;
        }
        else{
            return 0;
        }
    }

    private void vaciarCampos(){
        this.email.setText("");
        this.passw.setText("");
        this.passw2.setText("");
        this.respuestaSeg.setText("");
        this.nombre.setText("");
        this.apellidos.setText("");
        this.fechaNac.setText("");
    }

}
