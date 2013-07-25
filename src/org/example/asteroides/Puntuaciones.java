package org.example.asteroides;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Puntuaciones extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.puntuaciones);
		// asociar un layout definido a nuestro ListView
		setListAdapter(new MiAdaptador(this,
				Asteroides.almacen.listaPuntuaciones(10)));
	}

}
