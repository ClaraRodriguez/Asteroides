package org.example.asteroides;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Asteroides extends Activity implements OnGesturePerformedListener {

	private Button btnAcercaDe;
	private Button btnSalir;
	private Button btnConfig;
	private Button btnJuego;
	private TextView titulo;
	private GestureLibrary libreria;
	//public static AlmacenPuntuacionesArray almacen = new AlmacenPuntuacionesArray();
	private MediaPlayer mp;
	public static final int ACTIVIDAD_JUEGO = 1;
	private AlmacenPuntuacionesPreferencias almacen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnConfig = (Button) findViewById(R.id.button2);
		btnAcercaDe = (Button) findViewById(R.id.button3);
		btnSalir = (Button) findViewById(R.id.button4);
		btnJuego = (Button) findViewById(R.id.button1);
		titulo = (TextView) findViewById(R.id.textView1);

		// Animación del título

		Animation animacion = AnimationUtils.loadAnimation(this,
				R.anim.animacion);
		titulo.startAnimation(animacion);
		// Preferencias
		almacen = new AlmacenPuntuacionesPreferencias(this);

		// Reproductor de música ////////
		mp = MediaPlayer.create(this, R.raw.menu_theme);
		mp.start();

		// Evento botón "Acerca de" //////////////////////////
		btnAcercaDe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lanzarAcercaDe(null);

			}
		});

		// Evento botón "salir" //////////////////////////////
		btnSalir.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lanzarPuntuaciones(null);
			}
		});
		// Evento botón configuración
		btnConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lanzarPreferencias(null);
			}
		});

		// Evento botón juego
		btnJuego.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lanzarJuego(null);

			}
		});
		// Libreria para menú gestual
		libreria = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!libreria.load()) {
			finish();
		}
		// crea la "capa" sobre la que se escriben los gestos descritos en
		// "gestures" y
		// añade a esta activity
		GestureOverlayView gv = (GestureOverlayView) findViewById(R.id.gestures);
		gv.addOnGesturePerformedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.acercaDe:
			lanzarAcercaDe(null);
			break;

		case R.id.config:
			lanzarPreferencias(null);
			break;

		}
		return true;
	}

	public void lanzarAcercaDe(View view) {
		Intent i = new Intent(this, AcercaDe.class);
		startActivity(i);
	}

	public void lanzarPreferencias(View view) {
		Intent i = new Intent(this, Preferencias.class);
		startActivity(i);
	}

	public void mostrarPreferencias(View view) {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		String s = "Música: " + pref.getBoolean("musica", true)
				+ ", Conexion: " + pref.getString("jugadores", "1");

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();

	}

	public void lanzarPuntuaciones(View view) {
		Intent i = new Intent(this, Puntuaciones.class);
		startActivity(i);
	}

	public void lanzarJuego(View view) {
		Intent i = new Intent(this, Juego.class);
		startActivityForResult(i, ACTIVIDAD_JUEGO);

	}

	// Este método procesa la información recibida por los dibujos del usuario
	// en pantalla
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> prediction = libreria.recognize(gesture);
		if (prediction.size() > 0) {
			String comando = prediction.get(0).name;

			if (comando.equalsIgnoreCase("play")) {
				lanzarJuego(null);
			} else if (comando.equalsIgnoreCase("configurar")) {
				lanzarPreferencias(null);
			} else if (comando.equalsIgnoreCase("acerca_de")) {
				lanzarAcercaDe(null);
			} else if (comando.equalsIgnoreCase("cancelar")) {
				finish();
			}

		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mp != null) {
			mp.pause();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mp != null) {
			mp.start();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mp != null) {
			mp.release();
			mp = null;
		}
	}

	// guardar y recuperar el estado del reproductor cuando se destruye la actividad
	@Override
	protected void onSaveInstanceState(Bundle estadoGuardado) {
		super.onSaveInstanceState(estadoGuardado);
		if(mp != null){
			int pos = mp.getCurrentPosition();
			estadoGuardado.putInt("posicion", pos);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle estadoGuardado) {
		super.onRestoreInstanceState(estadoGuardado);
		if(estadoGuardado != null && mp != null){
			int pos = estadoGuardado.getInt("posicion");
			mp.seekTo(pos);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == ACTIVIDAD_JUEGO & resultCode == RESULT_OK & data != null){
			int puntuacion = data.getExtras().getInt("puntuacion");
			String nombre = "Yo";
			almacen.guardarPuntuacion(puntuacion, nombre, System.currentTimeMillis());
			lanzarPuntuaciones(null);
			
		}
	}
	
	

}
