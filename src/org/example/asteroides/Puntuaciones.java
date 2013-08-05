package org.example.asteroides;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Puntuaciones extends ListActivity {
	
	AlmacenPuntuacionesPreferencias almacen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		almacen = new AlmacenPuntuacionesPreferencias(this);
		setContentView(R.layout.puntuaciones);
		// asociar un layout definido a nuestro ListView
		setListAdapter(new MiAdaptador(this, almacen.listaPuntuaciones(10)));
	}

}
