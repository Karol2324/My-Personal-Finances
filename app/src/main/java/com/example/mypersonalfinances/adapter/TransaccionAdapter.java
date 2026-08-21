package com.example.mypersonalfinances.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypersonalfinances.FormularioActivity;
import com.example.mypersonalfinances.R;
import com.example.mypersonalfinances.model.Transaccion;

import java.util.List;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    private final List<Transaccion> listaTransacciones;

    public TransaccionAdapter(List<Transaccion> listaTransacciones) {
        this.listaTransacciones = listaTransacciones;
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion t = listaTransacciones.get(position);

        holder.tvConcepto.setText(t.getConcepto());
        holder.tvTipo.setText(t.getTipo());
        holder.tvMonto.setText("$ " + t.getMonto());

        // Colores e indicadores según el tipo
        if (t.esIngreso()) {
            holder.viewIndicador.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
            holder.tvMonto.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.viewIndicador.setBackgroundColor(Color.parseColor("#F44336")); // Rojo
            holder.tvMonto.setTextColor(Color.parseColor("#F44336"));
        }

        // Clic en el botón Editar o en toda la tarjeta
        View.OnClickListener abrirEdicion = v -> {
            Intent intent = new Intent(v.getContext(), FormularioActivity.class);
            intent.putExtra("id", t.getId());
            intent.putExtra("concepto", t.getConcepto());
            intent.putExtra("monto", t.getMonto());
            intent.putExtra("tipo", t.getTipo());
            v.getContext().startActivity(intent);
        };

        holder.itemView.setOnClickListener(abrirEdicion);
        holder.btnEditar.setOnClickListener(abrirEdicion);

        // Clic en el botón Eliminar (Abre el formulario directo a confirmar borrado)
        holder.btnEliminar.setOnClickListener(abrirEdicion);
    }

    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    public static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        View viewIndicador;
        TextView tvConcepto, tvTipo, tvMonto;
        ImageButton btnEditar, btnEliminar;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIndicador = itemView.findViewById(R.id.viewIndicadorTipo);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
