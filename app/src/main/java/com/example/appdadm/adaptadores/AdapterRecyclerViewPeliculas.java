package com.example.appdadm.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdadm.R;
import com.example.appdadm.Ventanas.PeliculaModel;
import com.example.appdadm.objetos.Pelicula;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

import java.util.ArrayList;

public class AdapterRecyclerViewPeliculas extends RecyclerView.Adapter<AdapterRecyclerViewPeliculas.ViewHolder> {

    private int REQUEST_CODE_ACTUALIZACION_PELICULA = 29;
    private ArrayList<Pelicula> arrayPelis;
    private LayoutInflater mInflater;
    private Context context;
    private Usuario usuAux;
    private DataBaseHelper db;


    public AdapterRecyclerViewPeliculas(ArrayList<Pelicula> arrayPlataf, Context context, Usuario usuAux) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayPelis = arrayPlataf;
        this.usuAux = usuAux;
        this.db = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recycler_peliculas,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecyclerViewPeliculas.ViewHolder holder, int position) {
        holder.binData(arrayPelis.get(position));

        // Asignar OnClickListener directamente al itemView
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener la plataforma seleccionada
                int adapterPosition = holder.getAdapterPosition();
                try {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Pelicula peliculaSeleccionada = arrayPelis.get(adapterPosition);
                        Intent intent = new Intent(context, PeliculaModel.class);
                        intent.putExtra("pelicula",peliculaSeleccionada);
                        intent.putExtra("usuario",usuAux);
                        // Iniciar la nueva actividad
                        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_ACTUALIZACION_PELICULA);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "ERROR AL ABRIT PELICULA", Toast.LENGTH_SHORT).show();
                    Log.d("ERROR AL ABRIT PELICULA","ERROR AL ABRIR PELICULA");
                }

            }
        });

        // Asignar OnLongClickListener al itemView
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // Realizar acciones cuando se hace clic largo en un elemento
                    // Por ejemplo, mostrar un diálogo de confirmación para eliminar el elemento
                    Pelicula peliculaSeleccionada = arrayPelis.get(adapterPosition);
                    mostrarDialogoEliminarPelicula(peliculaSeleccionada);
                    return true; // Indica que el evento de clic largo fue manejado
                }
                return false; // Indica que el evento de clic largo no fue manejado
            }
        });
    }

    // Método para mostrar un diálogo de confirmación para eliminar la película
    private void mostrarDialogoEliminarPelicula(Pelicula pelicula) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustomStyle);
        builder.setTitle("Eliminar película ?");
        builder.setMessage("¿Estás seguro de que quieres eliminar esta película?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Eliminar la película
                eliminarPelicula(pelicula);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    // Método para eliminar la película de la base de datos o de la lista
    private void eliminarPelicula(Pelicula pelicula) {
        // Eliminar la película de la base de datos
        db.eliminarPelicula(pelicula.getId()); // Supongamos que tienes un método en tu DataBaseHelper para eliminar una película por su ID

        // Eliminar la película de la lista
        arrayPelis.remove(pelicula);

        // Notificar al adaptador que los datos han cambiado
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return arrayPelis.size();
    }

    public void setItems(ArrayList<Pelicula> items){
        this.arrayPelis = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView titulo;
        TextView calificacion;
        TextView genero;
        TextView duracion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.imgModelPelicula);
            titulo = itemView.findViewById(R.id.txtVTituloPeli);
            calificacion = itemView.findViewById(R.id.txtVCalificacionPeli);
            genero = itemView.findViewById(R.id.txtVGeneroPeli);
            duracion = itemView.findViewById(R.id.txtVDuracionPeli);
        }

        void binData(final Pelicula item){

            try {
                titulo.setText(item.getTitulo());
                calificacion.setText(String.valueOf(item.getCalificacion()));
                genero.setText(item.getGenero());
                duracion.setText(item.getDuracionMinutos() + " min");
                if (item.getCaratuaUrl() != null){
                    Glide.with(context)
                            .load(item.getCaratuaUrl())
                            .into(iconImage);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
