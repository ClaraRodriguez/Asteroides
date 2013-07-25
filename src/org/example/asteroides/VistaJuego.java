package org.example.asteroides;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VistaJuego extends View {

	private Vector<Grafico> asteroides;
	private Drawable fondo1;
	private Drawable drawableNave;
	private Drawable asteroide1;
	private int numAsteroides = 5; // Numero inicial de asteroides
	private int numFragmentos = 3;

	// /////// NAVE ////////////////////////
	private Grafico nave;
	private int giroNave;
	private float aceleracionNave;
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

	public VistaJuego(Context context, AttributeSet attrs) {
		super(context, attrs);

		Resources res = context.getResources();
		asteroides = new Vector<Grafico>();
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
				// ActivaMisil();
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
	}

	class ThreadJuego extends Thread {

		@Override
		public void run() {
			while (true)
				actualizarFisica();
		}

	}

}
