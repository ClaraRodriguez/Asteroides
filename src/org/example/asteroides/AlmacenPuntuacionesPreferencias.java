package org.example.asteroides;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;

public class AlmacenPuntuacionesPreferencias implements AlmacenPuntuaciones {

	private static String PREFERENCIAS = "puntuaciones";
	private Context context;

	public AlmacenPuntuacionesPreferencias(Context context) {
		super();
		this.context = context;
	}

	@Override
	public void guardarPuntuacion(int puntos, String nombre, long fecha) {
		// TODO Auto-generated method stub
		SharedPreferences preferencias = context.getSharedPreferences(
				PREFERENCIAS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferencias.edit();

		for (int i = 9; i >= 1; i--) {
			editor.putString("puntuacion" + i,
					preferencias.getString("puntuacion" + (i - 1), ""));
		}

		editor.putString("puntuacion0", puntos + " " + nombre);

		editor.commit();
	}

	@Override
	public Vector<String> listaPuntuaciones(int cantidad) {
		Vector<String> result = new Vector<String>();
		SharedPreferences preferencias = context.getSharedPreferences(
				PREFERENCIAS, Context.MODE_PRIVATE);

		for (int i = 0; i < 9; i++) {
			String s = preferencias.getString("puntuacion" + i, "");
			if (!s.equals(""))
				result.add(s);
		}
		return result;
	}

}
