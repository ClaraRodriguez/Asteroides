package org.example.asteroides;

import java.util.Vector;



public class AlmacenPuntuacionesArray implements AlmacenPuntuaciones {
	
	private Vector<String> puntuaciones;
	
	

	public AlmacenPuntuacionesArray() {
		super();
		
		puntuaciones = new Vector<String>();
		puntuaciones.add("123000 Irene Francescon");
		puntuaciones.add("111000 Gordito pequeño");
	}

	@Override
	public void guardarPuntuacion(int puntos, String nombre, long fecha) {
		puntuaciones.add(0, puntos + " " + nombre);

	}

	@Override
	public Vector<String> listaPuntuaciones(int cantidad) {
		// TODO Auto-generated method stub
		return puntuaciones;
	}

}
