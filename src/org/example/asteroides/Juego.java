package org.example.asteroides;

import android.app.Activity;
import android.os.Bundle;

public class Juego extends Activity {

	private VistaJuego vistaJuego;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.juego);

		vistaJuego = (VistaJuego) findViewById(R.id.fondoJuego1);
		vistaJuego.setPadre(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		vistaJuego.getHilo().pausar();
		vistaJuego.stopSensorOrientacion();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		vistaJuego.regSensorOrientacion();
		vistaJuego.getHilo().reanudar();
	}

	@Override
	protected void onDestroy() {
		vistaJuego.getHilo().detener();
		super.onDestroy();
	}

}
