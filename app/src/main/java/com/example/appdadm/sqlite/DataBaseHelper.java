package com.example.appdadm.sqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.example.appdadm.objetos.Pelicula;
import com.example.appdadm.objetos.Plataforma;
import com.example.appdadm.objetos.Usuario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "miBaseDeDatos";
    private static int DATABASE_VERSION = 1;
    private Context context;  // Agrega un contexto al constructor

    private static final String CREATE_USUARIO_TABLE =
            "CREATE TABLE USUARIO (" +
                    "email TEXT, " +
                    "nombre TEXT NOT NULL, " +
                    "apellidos TEXT, " +
                    "fechaNac TEXT, " +
                    "preguntaSeg TEXT, " +
                    "respuestaSeg TEXT, " +
                    "intereses TEXT, " +
                    "segundoFactor INTEGER, " +
                    "password TEXT NOT NULL, " +
                    "PRIMARY KEY(email));";

    private static final String CREATE_PLATAFORMAS_TABLE =
            "CREATE TABLE PLATAFORMAS (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "idusuario TEXT NOT NULL, " +
                    "nombre TEXT, " +
                    "imagen TEXT, " +
                    "url TEXT, " +
                    "password TEXT, " +
                    "FOREIGN KEY(idusuario) REFERENCES USUARIO(email));";

    private static final String CREATE_PELICULAS_TABLE =
            "CREATE TABLE PELICULAS (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "idusuario TEXT NOT NULL, " +
                    "idplataforma TEXT NOT NULL, " +
                    "titulo TEXT, " +
                    "duracion String, " +
                    "genero TEXT, " +
                    "calificacion INTEGER, " +
                    "imagen TEXT, " +
                    "FOREIGN KEY(idusuario) REFERENCES USUARIO(email)," +
                    "FOREIGN KEY(idplataforma) REFERENCES PLATAFORMAS(id));";

    private static final String CREATE_TRIGGER_ELIMINAR_PELICULAS =
            "CREATE TRIGGER eliminar_peliculas_on_delete_plataforma " +
                    "AFTER DELETE ON PLATAFORMAS " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "DELETE FROM PELICULAS WHERE idplataforma = OLD.id; " +
                    "END;";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.w("AAAA", "ENTRA AL CONSTRUCTOR");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.w("AAAA", "ENTRA AL ONCREATE");
            db.execSQL(CREATE_PLATAFORMAS_TABLE);
            db.execSQL(CREATE_USUARIO_TABLE);
            db.execSQL(CREATE_PELICULAS_TABLE);
            db.execSQL(CREATE_TRIGGER_ELIMINAR_PELICULAS);
        }
        catch (Exception e){
            e.printStackTrace();
            Log.w("MiApp", "ERROR EN EL ONCREATE");
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, "Base de datos actualizada de la versión " + oldVersion + " a la versión " + newVersion, Toast.LENGTH_SHORT).show();
        this.DATABASE_VERSION = newVersion;
    }
    public long insertarUsuario(String email, String nombre, String apellidos, String fechaNac, String preguntaSeg, String respuestaSeg, String intereses, int segundoFactor, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("nombre", nombre);
        values.put("apellidos", apellidos);
        values.put("fechaNac", fechaNac);
        values.put("preguntaSeg", preguntaSeg);
        values.put("respuestaSeg", respuestaSeg);
        values.put("intereses", intereses);
        values.put("segundoFactor", segundoFactor);
        values.put("password", password);

        // Insertar el registro en la tabla
        long newRowId = db.insert("USUARIO", null, values);

        // Cierra la base de datos después de usarla
        db.close();

        return newRowId;
    }

    // En tu clase DataBaseHelper
    public long insertarPlataforma(Plataforma plataforma) {
        SQLiteDatabase db;
        long newRowId = -1;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idusuario", plataforma.getIdUsuario());
            values.put("nombre", plataforma.getNombre());
            values.put("imagen", plataforma.getImageUrl());
            values.put("url", plataforma.getUrl());
            values.put("password", plataforma.getPassword());
            newRowId = db.insert("PLATAFORMAS", null, values);
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
            Log.w("PETARDASSO", "Exception: " + e.getMessage());
        }
        return newRowId;
    }

    public int updatePlataforma(Plataforma plataforma) {
        SQLiteDatabase db;
        int numRowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", plataforma.getNombre());
            values.put("imagen", plataforma.getImageUrl());
            values.put("url", plataforma.getUrl());
            values.put("password", plataforma.getPassword());
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(plataforma.getId())};
            numRowsAffected = db.update("PLATAFORMAS", values, whereClause, whereArgs);
            Log.w("ACABAA UPDATE", "Exception: ");
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("PETARDASSO", "Exception: " + e.getMessage());
        }
        return numRowsAffected;
    }


    public long insertarPelicula(Pelicula pelicula) {
        SQLiteDatabase db;
        long newRowId = -1;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("idusuario", pelicula.getIdUsuario());
            values.put("idplataforma", pelicula.getIdPlataforma());
            values.put("titulo", pelicula.getTitulo());
            values.put("duracion", pelicula.getDuracionMinutos());
            values.put("genero", pelicula.getGenero());
            values.put("calificacion", pelicula.getCalificacion());
            values.put("imagen", pelicula.getCaratuaUrl());
            newRowId = db.insert("PELICULAS", null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newRowId;
    }

    public int updatePelicula(Pelicula pelicula) {
        SQLiteDatabase db;
        int numRowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("titulo", pelicula.getTitulo());
            values.put("duracion", pelicula.getDuracionMinutos());
            values.put("genero", pelicula.getGenero());
            values.put("calificacion", pelicula.getCalificacion());
            values.put("imagen", pelicula.getCaratuaUrl());
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(pelicula.getId())};
            numRowsAffected = db.update("PELICULAS", values, whereClause, whereArgs);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("PETARDASSO", "Exception: " + e.getMessage());
        }
        return numRowsAffected;
    }


    /**
     * Metood para obtener todas las plataformas que tenga el usuario
     * @param usu
     * @return
     */
    public ArrayList<Plataforma> getAllPlataformas(Usuario usu) {
        ArrayList<Plataforma> plataformas = new ArrayList<>();
        String selectQuery = "SELECT * FROM PLATAFORMAS WHERE idusuario = ? ORDER BY nombre ASC";
        Cursor cursor = null;
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            cursor = db.rawQuery(selectQuery, new String[]{usu.getEmail()});
            int indexNombre = cursor.getColumnIndex("nombre");
            int indexUrl = cursor.getColumnIndex("url");
            int indexId = cursor.getColumnIndex("id");
            int indexIdUsuario = cursor.getColumnIndex("idusuario");
            int indexImg = cursor.getColumnIndex("imagen");

            if (cursor.moveToFirst()) {
                do {
                    Plataforma plataforma = new Plataforma();
                    plataforma.setNombre(cursor.getString(indexNombre));
                    plataforma.setUrl(cursor.getString(indexUrl));
                    plataforma.setId(cursor.getInt(indexId));
                    plataforma.setIdUsuario(cursor.getString(indexIdUsuario));
                    plataforma.setImageUrl(cursor.getString(indexImg));
                    plataformas.add(plataforma);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return plataformas;
    }
    /**
     * Metodo para obtener las peliculas de x plataforma que el usuario tenga
     * @param usuEmail
     * @param idPlataforma
     * @return
     */
    public ArrayList<Pelicula> getAllPeliculasFromUserPlatform(String usuEmail, String idPlataforma) {
        ArrayList<Pelicula> pelis = new ArrayList<>();
        String selectQuery = "SELECT * FROM PELICULAS WHERE idusuario = ? AND idplataforma = ?";
        Cursor cursor = null;
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            cursor = db.rawQuery(selectQuery, new String[]{usuEmail, idPlataforma});  // Utiliza los parámetros proporcionados

            int indexTitulo = cursor.getColumnIndex("titulo");
            int indexDuracion = cursor.getColumnIndex("duracion");
            int indexCalificacion = cursor.getColumnIndex("calificacion");
            int indexGenero = cursor.getColumnIndex("genero");
            int indexId = cursor.getColumnIndex("id");
            int indexImg = cursor.getColumnIndex("imagen");

            if (cursor.moveToFirst()) {
                do {
                    Pelicula peli = new Pelicula();
                    peli.setGenero(cursor.getString(indexGenero));
                    peli.setTitulo(cursor.getString(indexTitulo));
                    peli.setDuracionMinutos(cursor.getString(indexDuracion));
                    peli.setCalificacion(cursor.getInt(indexCalificacion));
                    peli.setId(cursor.getInt(indexId));
                    peli.setCaratuaUrl(cursor.getString(indexImg));
                    pelis.add(peli);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROOOOOREXCEPTION", "Exception: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return pelis;
    }

    public ArrayList<Pelicula> getAllPeliculasFromUser(String usuEmail) {

        ArrayList<Pelicula> pelis = new ArrayList<>();
        String selectQuery = "SELECT * FROM PELICULAS WHERE idusuario = ?";
        Cursor cursor = null;
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            cursor = db.rawQuery(selectQuery, new String[]{usuEmail});

            int indexTitulo = cursor.getColumnIndex("titulo");
            int indexDuracion = cursor.getColumnIndex("duracion");
            int indexCalificacion = cursor.getColumnIndex("calificacion");
            int indexGenero = cursor.getColumnIndex("genero");
            int indexId = cursor.getColumnIndex("id");
            int indexImg = cursor.getColumnIndex("imagen");

            if (cursor.moveToFirst()) {
                do {
                    Pelicula peli = new Pelicula();
                    peli.setGenero(cursor.getString(indexGenero));
                    peli.setTitulo(cursor.getString(indexTitulo));
                    peli.setDuracionMinutos(cursor.getString(indexDuracion));
                    peli.setCalificacion(cursor.getInt(indexCalificacion));
                    peli.setId(cursor.getInt(indexId));
                    peli.setCaratuaUrl(cursor.getString(indexImg));
                    pelis.add(peli);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROOOOOREXCEPTION", "Exception: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return pelis;
    }

    public void  eliminarPelicula(int peliculaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("PELICULAS", "id=?", new String[]{String.valueOf(peliculaId)});
        db.close();
    }
    public void eliminarPlataforma(int idPlataforma) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = {String.valueOf(idPlataforma)};
        db.delete("PLATAFORMAS", "id = ?", whereArgs);
        db.close();
    }




}
