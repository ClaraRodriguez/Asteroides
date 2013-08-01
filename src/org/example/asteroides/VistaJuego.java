package org.example.asteroides;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.SyncResult;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VistaJuego extends View implements SensorEventListener {

	private Vector<Grafico> asteroides;
	private Vector<Misil> misiles;
	private Drawable fondo1;
	private Drawable drawableNave;
	private Drawable asteroide1;
	private int numAsteroides = 5; // Numero inicial de asteroides
	private int numMisiles = 10; // numero de misiles
	private int numFragmentos = 3;

	// /////// NAVE ////////////////////////
	private Grafico nave;
	private int giroNave;
	private float aceleracionNave;

	// ////// MISIL ///////////////////////
	// Misil misil;
	Iterator vecInterator;

	// Incremento de giro y aceleración
	private static final int PASO_GIRO_NAVE = 5;
	private static final float PASO_ACELERACION_NAVE = 0.5f;

	private float mX = 0, mY = 0;
	private boolean disparo = false;

	// ////////// THREAD Y TIEMPO //////////////////
	// Hilo de ejecución del juego
	private ThreadJuego hilo = new ThreadJuego();
	// Cada cuánto queremos actualizar los cambios
	private static int PERIODO_PROCESO = 50;
	// cuándo se realizo el último proceso: 'delta Time'
	private long ultimoProceso = 0;

	// VALORES SENSORES ///////////
	SensorManager sm;
	private boolean isValorInicial;
	private float valorInicial;
	private boolean sensorActivo = false;

	// MULTIMEDIA /////////////////
	SoundPool soundPool;
	int idDisparo, idExplosion;

	public VistaJuego(Context context, AttributeSet attrs) {
		super(context, attrs);

		Resources res = context.getResources();
		asteroides = new Vector<Grafico>();
		misiles = new Vector<Misil>();
		fondo1 = res.getDrawable(R.drawable.grid);
		asteroide1 = res.getDrawable(R.drawable.asteroide1);
		for (int i = 0; i < numAsteroides; i++) {
			Grafico asteroide = new Grafico(this, asteroide1);

			asteroide.setIncX(Math.random() * 4 - 2); // rango de -2 a 2 //
														// (velocidad)
			asteroide.setIncY(Math.random() * 4 - 2);
			asteroide.setAngulo((int) (Math.random() * 360));
			asteroide.setRotación((int) (Math.random() * 8 - 4));
			asteroides.add(asteroide);
		}
		nave = new Grafico(this, res.getDrawable(R.drawable.nave));
		// Creación de misiles
		for (int i = 0; i < numMisiles; i++) {
			Misil misil = new Misil(this, res.getDrawable(R.drawable.misil1));
			misiles.add(misil);
		}
		vecInterator = misiles.iterator();

		// Registramos el sensor e indicamos qué objeto recogerá la llamada
		// callback
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		regSensorOrientacion();

		// Iniciamos el SoundPool y los disparos
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		idDisparo = soundPool.load(context, R.raw.disparo, 0);
		idExplosion = soundPool.load(context, R.raw.explosion, 0);

	}

	@Override
	protected void onSizeChanged(int ancho, int alto, int anchoAnterior,
			int altoAnterior) {

		super.onSizeChanged(ancho, alto, anchoAnterior, altoAnterior);

		fondo1.setBounds(0, 0, ancho, alto);
		for (Grafico asteroide : asteroides) {
			do {
				// evita que los asteroides coincidan con la nave al inicio
				asteroide.setPosX(Math.random()
						* (ancho - asteroide.getAncho()));
				asteroide.setPosY(Math.random() * (alto - asteroide.getAlto()));
			} while (nave.verificarColision(asteroide));

		}

		nave.setPosX(ancho / 2);
		nave.setPosY(alto / 2);

		ultimoProceso = System.currentTimeMillis();
		hilo.start();

	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		fondo1.draw(canvas);
		for (Grafico asteroide : asteroides) {
			asteroide.dibujaGrafico(canvas);
		}

		nave.dibujaGrafico(canvas);
		for (Misil misil : misiles) {
			if (misil.isMisilActivo()) {
				misil.dibujaGrafico(canvas);
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		try {
			Thread.sleep(60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			disparo = true;
			break;
		case MotionEvent.ACTION_MOVE:
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dy < 6 && dx > 6) {
				giroNave = Math.round((x - mX) / 25);
				disparo = false;
			} else if (dx < 6 && dy > 6) {
				aceleracionNave = Math.round((mY - y) / 25);
				disparo = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			giroNave = 0;
			aceleracionNave = 0;
			if (disparo) {
				// Disparo de varios misiles en pantalla
				while (vecInterator.hasNext()) {
					Misil misil = (Misil) vecInterator.next();
					if (!misil.isMisilActivo()) {
						activaMisil(misil);
						break;
					}

				}

			}
			break;
		default:
			break;
		}
		mX = x;
		mY = y;
		return true;

	}

	protected synchronized void actualizarFisica() {

		long ahora = System.currentTimeMillis();
		// No hacer nada si el periodo de proceso no se ha cumplido
		if (ahora < ultimoProceso + PERIODO_PROCESO) {
			return;
		}

		// Para una ejecución en tiempo real calculamos retardo
		double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
		ultimoProceso = ahora; // Para la próxima vez
		// actualizamos velocidad y dirección de la nave a partir
		// de giroNave y aceleracionNave (según la entrada del usuario)
		nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
		double nIncX = nave.getIncX() + aceleracionNave
				* Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
		double nIncY = nave.getIncY() + aceleracionNave
				* Math.sin(Math.toRadians(nave.getAngulo())) * retardo;

		if (Math.hypot(nIncX, nIncY) <= Grafico.getMaxVelocidad()) {
			nave.setIncX(nIncX);
			nave.setIncY(nIncY);
		}
		// Incrementamos la posición de la nave y de los asteroides
		nave.incrementaPos(retardo);

		for (Grafico asteroide : asteroides) {
			asteroide.incrementaPos(retardo);
		}

		// Actualizamos posición del misil //////
		for (Misil misil : misiles) {
			if (misil.isMisilActivo()) {
				misil.incrementaPos(retardo);

				misil.setTiempoMisil(misil.getTiempoMisil() - (int) retardo);
				if (misil.getTiempoMisil() < 0) {
					misil.setMisilActivo(false);
				} else {
					for (int i = 0; i < asteroides.size(); i++) {
						if (misil.verificarColision(asteroides.elementAt(i))) {
							destruyeAsteroide(i, misil);
							break;
						}
					}
				}
			}
		}
	}

	private void destruyeAsteroide(int i, Misil misil) {
		asteroides.remove(i);
		misil.setMisilActivo(false);
		soundPool.play(idExplosion, 1, 1, 0, 0, 1);

	}

	private void activaMisil(Misil misil) {
		// centramos el misil atendiendo a la posición, altura y anchura de la
		// nave

		misil.setPosX(nave.getPosX() + nave.getAncho() / 2 - misil.getAncho()
				/ 2);
		misil.setPosY(nave.getPosY() + nave.getAlto() / 2 - misil.getAlto() / 2);

		misil.setAngulo(nave.getAngulo());
		misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo()))
				* Misil.PASO_VELOCIDAD_MISIL);
		misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo()))
				* Misil.PASO_VELOCIDAD_MISIL);
		/*
		 * Damos un tiempo de vida al misil para que no esté recorriendo
		 * permanentemente la pantalla hasta chocar con un asteroide. Nos
		 * quedamos con el mínimo entre el ancho dividido por la velocidad y el
		 * alto dividido por la velocidad y le restamos una constante
		 */

		misil.setTiempoMisil((int) Math.min(
				this.getWidth() / Math.abs(misil.getIncX()), this.getHeight()
						/ Math.abs(misil.getIncY()))
				- Misil.TIEMPO_VIDA);

		misil.setMisilActivo(true);
		soundPool.play(idDisparo, 1, 1, 1, 0, 1);

	}

	class ThreadJuego extends Thread {

		private boolean pausa, corriendo;

		public synchronized void pausar() {
			pausa = true;
		}

		public synchronized void reanudar() {
			pausa = false;
			notify();
		}

		public void detener() {
			corriendo = false;
			if (pausa)
				reanudar();
		}

		@Override
		public void run() {
			corriendo = true;
			while (corriendo) {
				actualizarFisica();
				synchronized (this) {
					while (pausa) {
						try {
							// Detenemos la actividad cuando ésta entre en pausa
							wait();
						} catch (Exception e) {

						}
					}
				}
			}

		}

	}

	// Métodos para habilitar los sensores de orientación
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

		this.isValorInicial = false;

	}

	// Registro del sensor
	public boolean regSensorOrientacion() {
		List<Sensor> listaSensores = sm.getSensorList(Sensor.TYPE_ORIENTATION);
		if (!listaSensores.isEmpty()) {
			Sensor sensorOrientacion = listaSensores.get(0);
			sm.registerListener(this, sensorOrientacion, sm.SENSOR_DELAY_GAME);
			return sensorActivo = true;
		} else
			return sensorActivo;

	}

	// Desactivar sensor
	public boolean stopSensorOrientacion() {
		if (sensorActivo) {
			sm.unregisterListener(this);
			sensorActivo = false;
		}
		return sensorActivo;
	}

	// Método implementado de la interfaz SensorEventListener
	@Override
	public void onSensorChanged(SensorEvent event) {
		float valor = event.values[1];
		if (!isValorInicial) {
			valorInicial = valor;
			isValorInicial = true;
		}
		giroNave = (int) (valor - valorInicial) / 3;
	}

	// Método getter de ThreadJuego para controlar el hilo fuera de VistaJuego
	public ThreadJuego getHilo() {
		return hilo;
	}

	public SensorManager getSm() {
		return sm;
	}

}
