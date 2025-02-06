package com.example.appdadm.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.appdadm.Ventanas.PlataformaModel;
import com.example.appdadm.objetos.Plataforma;
import com.example.appdadm.objetos.Usuario;
import com.example.appdadm.sqlite.DataBaseHelper;

import java.util.ArrayList;

public class AdapterRecyclerViewPlataformas extends RecyclerView.Adapter<AdapterRecyclerViewPlataformas.ViewHolder> {

    public interface OnPlataformaDeletedListener {
        void onPlataformaDeleted(Plataforma plataforma);
    }

    private OnPlataformaDeletedListener listener;

    public void setOnPlataformaDeletedListener(OnPlataformaDeletedListener listener) {
        this.listener = listener;
    }

    private int REQUEST_CODE_ACTUALIZACION = 13;
    private ArrayList<Plataforma> arrayPlat;
    private LayoutInflater mInflater;
    private Context context;
    private Usuario usuAux;
    private DataBaseHelper db;

    public AdapterRecyclerViewPlataformas(ArrayList<Plataforma> arrayPlataf, Usuario usu, Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayPlat = arrayPlataf;
        this.usuAux = usu;
        this.db = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recycler_plataformas,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecyclerViewPlataformas.ViewHolder holder, int position) {
        holder.binData(arrayPlat.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Obtener la plataforma seleccionada
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Plataforma plataformaSeleccionada = arrayPlat.get(adapterPosition);
                        Intent intent = new Intent(context, PlataformaModel.class);
                        intent.putExtra("plataforma", plataformaSeleccionada);
                        intent.putExtra("usuario", usuAux);
                        // Iniciar la nueva actividad
                        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_ACTUALIZACION);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "FALLO AL ABRIR LA PLATAFORMA", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Plataforma plataformaSeleccionada = arrayPlat.get(adapterPosition);
                    mostrarDialogoEliminarPlataforma(plataformaSeleccionada);
                    return true; // Indica que el evento de clic largo fue manejado
                }
                return false; // Indica que el evento de clic largo no fue manejado
            }
        });
    }


    private void mostrarDialogoEliminarPlataforma(Plataforma plataf) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustomStyle);
        builder.setTitle("Eliminar plataforma ?");
        builder.setMessage("¿Estás seguro de que quieres eliminar esta plataforma y todas sus peliculas?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Eliminar la película
                eliminarPlataforma(plataf);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Método para eliminar la película de la base de datos o de la lista
    private void eliminarPlataforma(Plataforma plataf) {
        // Eliminar la plataforma de la base de datos
        db.eliminarPlataforma(plataf.getId());

        // Eliminar la plataforma de la lista de plataformas
        arrayPlat.remove(plataf);

        // Notificar a la actividad que se ha eliminado una plataforma
        if (listener != null) {
            listener.onPlataformaDeleted(plataf);
        }
    }

    @Override
    public int getItemCount() {
        return arrayPlat.size();
    }

    public void setItems(ArrayList<Plataforma> items){
        this.arrayPlat = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView nombre;
        TextView url;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.imgModelPlataforma);
            nombre = itemView.findViewById(R.id.nombrePlataforma);
            url = itemView.findViewById(R.id.urlPlataforma);
        }

        void binData(final Plataforma item){
            try {
                nombre.setText(item.getNombre());
                url.setText(item.getUrl());
                if (item.getImageUrl() != null){
                    Glide.with(context)
                            .load(item.getImageUrl())
                            .into(iconImage);
                }
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context, "FALLO ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
