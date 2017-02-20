package intentoAutoID;

import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

public class Logica implements Observer {
	private PApplet app;
	private Comunicacion conexion;

	public Logica(PApplet app) {
		this.app = app;
		conexion = new Comunicacion();
		Thread nt = new Thread(conexion);
		nt.start();
		
		conexion.addObserver(this);
	}

	public void pintar() {
		app.noStroke();
		app.fill(150);
		app.ellipse(100, 100, 40, 40);
		app.fill(255);
		app.text(conexion.getId(), 100, 100);
	}

	@Override
	public void update(Observable o, Object arg) {

	}
}