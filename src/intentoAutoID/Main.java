package intentoAutoID;

import processing.core.PApplet;

public class Main extends PApplet{
	
	private Logica log;
	
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		PApplet.main("ejemploAutoID.Main");
	}
	
	@Override
	public void settings(){
		size(600,200);
	}
	
	@Override
	public void setup(){
		log = new Logica(this);
	}
	
	@Override
	public void draw(){
		background(0);
		log.pintar();
	}

}

