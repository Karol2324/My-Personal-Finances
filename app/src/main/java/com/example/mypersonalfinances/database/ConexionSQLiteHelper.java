package com.example.mypersonalfinances.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mypersonalfinances.model.Transaccion;

import java.util.ArrayList;

/**
 * Gestiona la conexión con SQLite y encapsula las operaciones CRUD
 * sobre la tabla 'transacciones'.
 */
public class ConexionSQLiteHelper extends SQLiteOpenHelper {

    /** Nombre del archivo de base de datos en el dispositivo. */
    private static final String NOMBRE_BASE_DATOS = "mis_finanzas.db";

    /** Versión del esquema; incrementar si cambia la estructura de tablas. */
    private static final int VERSION_BASE_DATOS = 1;

    /** Nombre de la tabla principal de la aplicación. */
    public static final String TABLA_TRANSACCIONES = "transacciones";

    /** Constantes de columnas: evitan errores de tipeo en consultas SQL. */
    public static final String COL_ID = "id";
    public static final String COL_CONCEPTO = "concepto";
    public static final String COL_MONTO = "monto";
    public static final String COL_TIPO = "tipo";

    /** Sentencia DDL para crear la tabla al instalar la app por primera vez. */
    private static final String SQL_CREAR_TABLA =
            "CREATE TABLE " + TABLA_TRANSACCIONES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CONCEPTO + " TEXT NOT NULL, " +
                    COL_MONTO + " REAL NOT NULL, " +
                    COL_TIPO + " TEXT NOT NULL" +
                    ")";

    public ConexionSQLiteHelper(Context contexto) {
        super(contexto, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    /**
     * Se ejecuta una sola vez cuando la base de datos no existe aún.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREAR_TABLA);
    }

    /**
     * Se ejecuta si VERSION_BASE_DATOS aumenta (migraciones futuras).
     * Por ahora recrea la tabla desde cero.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_TRANSACCIONES);
        onCreate(db);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE — Insertar una nueva transacción
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inserta una transacción en la base de datos.
     *
     * @param transaccion Objeto con concepto, monto y tipo (el id se genera solo).
     * @return El id autogenerado, o -1 si la inserción falló.
     */
    public long insertarTransaccion(Transaccion transaccion) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(COL_CONCEPTO, transaccion.getConcepto());
        valores.put(COL_MONTO, transaccion.getMonto());
        valores.put(COL_TIPO, transaccion.getTipo());

        long idGenerado = db.insert(TABLA_TRANSACCIONES, null, valores);
        db.close();

        return idGenerado;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — Obtener todas las transacciones
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lee todas las transacciones ordenadas por id descendente (más recientes primero).
     *
     * @return Lista en memoria con todos los registros de la tabla.
     */
    public ArrayList<Transaccion> obtenerTransacciones() {
        ArrayList<Transaccion> listaTransacciones = new ArrayList<>();

        String consulta = "SELECT * FROM " + TABLA_TRANSACCIONES +
                " ORDER BY " + COL_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(consulta, null);

        if (cursor.moveToFirst()) {
            do {
                Transaccion transaccion = mapearCursorATransaccion(cursor);
                listaTransacciones.add(transaccion);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return listaTransacciones;
    }

    /**
     * Busca una transacción por su identificador (útil al editar desde el formulario).
     *
     * @param id Identificador de la transacción.
     * @return La transacción encontrada, o null si no existe.
     */
    public Transaccion obtenerTransaccionPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_TRANSACCIONES,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Transaccion transaccion = null;
        if (cursor.moveToFirst()) {
            transaccion = mapearCursorATransaccion(cursor);
        }

        cursor.close();
        db.close();

        return transaccion;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE — Actualizar una transacción existente
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Actualiza los campos de una transacción identificada por su id.
     *
     * @param transaccion Objeto con id y datos modificados.
     * @return Número de filas afectadas (1 si tuvo éxito, 0 si no se encontró).
     */
    public int actualizarTransaccion(Transaccion transaccion) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(COL_CONCEPTO, transaccion.getConcepto());
        valores.put(COL_MONTO, transaccion.getMonto());
        valores.put(COL_TIPO, transaccion.getTipo());

        int filasActualizadas = db.update(
                TABLA_TRANSACCIONES,
                valores,
                COL_ID + " = ?",
                new String[]{String.valueOf(transaccion.getId())}
        );

        db.close();

        return filasActualizadas;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — Eliminar una transacción
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Elimina una transacción de la base de datos por su id.
     *
     * @param id Identificador del registro a eliminar.
     * @return Número de filas eliminadas (1 si tuvo éxito, 0 si no existía).
     */
    public int eliminarTransaccion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int filasEliminadas = db.delete(
                TABLA_TRANSACCIONES,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return filasEliminadas;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidad interna: convertir fila del Cursor en objeto Transaccion
    // ─────────────────────────────────────────────────────────────────────────

    private Transaccion mapearCursorATransaccion(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String concepto = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONCEPTO));
        double monto = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_MONTO));
        String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO));

        return new Transaccion(id, concepto, monto, tipo);
    }
}
