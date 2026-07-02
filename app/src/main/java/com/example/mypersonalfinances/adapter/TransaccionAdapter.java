package com.example.mypersonalfinances.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypersonalfinances.R;
import com.example.mypersonalfinances.databinding.ItemTransaccionBinding;
import com.example.mypersonalfinances.model.Transaccion;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador personalizado que conecta la lista de {@link Transaccion}
 * con el RecyclerView de la pantalla principal.
 *
 * Implementa el patrón ViewHolder para reutilizar vistas y mejorar el rendimiento.
 */
public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    /** Lista de datos que se muestra en el RecyclerView. */
    private List<Transaccion> listaTransacciones;

    /** Listener para delegar acciones de editar/eliminar a la Activity. */
    private final OnTransaccionClickListener listener;

    /**
     * Contrato de comunicación entre el Adapter y la Activity (MainActivity).
     * La Activity implementa esta interfaz para ejecutar la lógica CRUD.
     */
    public interface OnTransaccionClickListener {

        /** Se invoca al pulsar el botón editar de una fila. */
        void onEditarClick(Transaccion transaccion);

        /** Se invoca al pulsar el botón eliminar de una fila. */
        void onEliminarClick(Transaccion transaccion);
    }

    public TransaccionAdapter(List<Transaccion> listaTransacciones, OnTransaccionClickListener listener) {
        this.listaTransacciones = new ArrayList<>(listaTransacciones);
        this.listener = listener;
    }

    /**
     * Crea una nueva vista inflando el layout de fila mediante ViewBinding.
     * Se llama cuando el RecyclerView necesita un ViewHolder nuevo.
     */
    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransaccionBinding binding = ItemTransaccionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TransaccionViewHolder(binding);
    }

    /**
     * Vincula los datos de una transacción con las vistas del ViewHolder.
     * Se llama para cada fila visible en pantalla.
     */
    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion transaccion = listaTransacciones.get(position);
        holder.enlazar(transaccion, listener);
    }

    /** Retorna el número total de ítems en la lista. */
    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    /**
     * Reemplaza el contenido del adaptador y refresca la interfaz.
     * MainActivity lo usará tras leer datos de SQLite en onResume().
     */
    public void actualizarLista(List<Transaccion> nuevaLista) {
        this.listaTransacciones = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder: mantiene referencias a las vistas de una fila
     * para evitar llamadas repetidas a findViewById.
     */
    static class TransaccionViewHolder extends RecyclerView.ViewHolder {

        private final ItemTransaccionBinding binding;

        TransaccionViewHolder(ItemTransaccionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Asigna los valores del modelo a los componentes visuales de la fila.
         */
        void enlazar(Transaccion transaccion, OnTransaccionClickListener listener) {
            Context contexto = binding.getRoot().getContext();

            // Datos de texto
            binding.tvConcepto.setText(transaccion.getConcepto());
            binding.tvTipo.setText(transaccion.getTipo());
            binding.tvMonto.setText(
                    contexto.getString(R.string.formato_monto, transaccion.getMonto())
            );

            // Estilos visuales según el tipo de movimiento
            if (transaccion.esIngreso()) {
                aplicarEstiloIngreso(contexto);
            } else {
                aplicarEstiloGasto(contexto);
            }

            // Clic en editar: notifica a MainActivity con la transacción seleccionada
            binding.btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(transaccion);
                }
            });

            // Clic en eliminar: notifica a MainActivity para confirmar y borrar
            binding.btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(transaccion);
                }
            });
        }

        /** Aplica colores y fondos para transacciones de tipo Ingreso. */
        private void aplicarEstiloIngreso(Context contexto) {
            int colorVerde = ContextCompat.getColor(contexto, R.color.verde_ingreso);

            binding.viewIndicadorTipo.setBackgroundColor(colorVerde);
            binding.tvTipo.setBackgroundResource(R.drawable.bg_badge_ingreso);
            binding.tvTipo.setTextColor(colorVerde);
            binding.tvMonto.setTextColor(colorVerde);
        }

        /** Aplica colores y fondos para transacciones de tipo Gasto. */
        private void aplicarEstiloGasto(Context contexto) {
            int colorRojo = ContextCompat.getColor(contexto, R.color.rojo_gasto);

            binding.viewIndicadorTipo.setBackgroundColor(colorRojo);
            binding.tvTipo.setBackgroundResource(R.drawable.bg_badge_gasto);
            binding.tvTipo.setTextColor(colorRojo);
            binding.tvMonto.setTextColor(colorRojo);
        }
    }
}
