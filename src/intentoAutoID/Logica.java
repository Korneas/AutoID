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
		app.ellipse(50, 100, 40, 40);
		app.fill(0);
		app.text(conexion.getId(), 50, 100);
	}

	@Override
	public void update(Observable o, Object arg) {

	}
}