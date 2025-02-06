package com.example.appdadm.Ventanas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdadm.R;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

public class LogIn extends AppCompatActivity {

    EditText pass;
    EditText email;
    DataBaseHelper dbHelper;
    Usuario usuAux;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DataBaseHelper(this);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        setContentView(R.layout.activity_login);

        TextView textViewRegister = findViewById(R.id.tWResgistrarse);

        Button btnIniciar = findViewById(R.id.btnIniciar);

        email = findViewById(R.id.eTCorreo);

        pass = findViewById(R.id.eTPassword);

        // Asignar un click listener al TextView
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogIn.this, Registro.class));
            }
        });

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = email.getText().toString();
                String contraseña = pass.getText().toString();

                // Comprobar si el usuario existe en la base de datos
                if (usuarioExiste(correo, contraseña)) {
                    // El usuario existe, puedes realizar acciones adicionales (por ejemplo, iniciar sesión)

                    Intent intent = new Intent(LogIn.this,Home.class);
                    usuAux = buscarUsuarioPorCorreo(correo);
                    intent.putExtra("USUARIO_AUX",usuAux);
                    startActivity(intent);
                    vaciarCampos();
                    Toast.makeText(LogIn.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                } else {
                    // El usuario no existe o las credenciales son incorrectas
                    Toast.makeText(LogIn.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    // Método para comprobar si un usuario existe en la base de datos
    private boolean usuarioExiste(String correo, String contraseña) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Realizar una consulta SELECT con el correo como condición
        String consulta = "SELECT * FROM USUARIO WHERE email = ? AND password = ?";
        String[] args = {correo, contraseña};

        Cursor cursor = db.rawQuery(consulta, args);

        // Verificar si el cursor tiene algún resultado
        boolean existe = cursor.getCount() > 0;

        // Cerrar el cursor y la base de datos después de usarlos
        cursor.close();
        db.close();

        return existe;
    }

    public Usuario buscarUsuarioPorCorreo(String correo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Realizar una consulta SELECT con el correo como condición
        String consulta = "SELECT * FROM USUARIO WHERE email = ?";
        String[] args = {correo};

        Cursor cursor = db.rawQuery(consulta, args);

        Usuario usuario = null;

        // Verificar si el cursor tiene algún resultado
        if (cursor.moveToFirst()) {
            // El cursor tiene al menos una fila, obtener datos
            usuario = new Usuario();

            try {
                // Acceder a los datos utilizando getColumnIndexOrThrow
                usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                usuario.setApellidos(cursor.getString(cursor.getColumnIndexOrThrow("apellidos")));
                usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                usuario.setPreguntaSeg(cursor.getString(cursor.getColumnIndexOrThrow("preguntaSeg")));
                usuario.setRespuestaSeg(cursor.getString(cursor.getColumnIndexOrThrow("respuestaSeg")));
                usuario.setIntereses(cursor.getString(cursor.getColumnIndexOrThrow("intereses")));
                usuario.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
                usuario.setDobleFactor(cursor.getInt(cursor.getColumnIndexOrThrow("segundoFactor")));

            } catch (Exception e) {
                e.printStackTrace();
                // Manejar la excepción, por ejemplo, registrándola o lanzando un nuevo error.
            }
        }

        // Cerrar el cursor y la base de datos después de usarlos
        cursor.close();
        db.close();

        return usuario;
    }


    private void vaciarCampos(){
            this.email.setText("");
            this.pass.setText("");
    }
}
