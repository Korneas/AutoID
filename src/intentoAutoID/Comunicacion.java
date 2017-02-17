package intentoAutoID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;

public class Comunicacion extends Observable implements Runnable {

	private MulticastSocket mSocket;
	private final int PORT = 5000;
	private final String GROUP_ADDRESS = "228.5.6.7";
	private boolean life;
	private boolean identificado;
	private int id;

	public Comunicacion() {
		life = true;

		try {
			mSocket = new MulticastSocket(PORT);
			InetAddress grupo = InetAddress.getByName(GROUP_ADDRESS);
			mSocket.joinGroup(grupo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		autoID();

	}

	private void autoID() {
		//Enviar saludo
		enviar(serialize(new MensajeID("Hola, soy nuevo")), GROUP_ADDRESS, 5000);

		
		try {
			mSocket.setSoTimeout(1000);
			while (!identificado) {
				//Si no se ha identificado se realiza el recibimiento de mensaje
				recibirRes();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void recibirRes() throws IOException {
		try {
			DatagramPacket dPacket = recibir();

			Object objRecibido = deserialize(dPacket.getData());

			if (objRecibido instanceof MensajeID) {
				MensajeID msg = (MensajeID) objRecibido;
				String cont = msg.getContenido();
				System.out.println("Contenido es:"+cont);
				
				if(cont.contains("nuevo")){
					dPacket = recibir();
					objRecibido = deserialize(dPacket.getData());
					
					msg = (MensajeID) objRecibido;
					cont = (String) msg.getContenido();
					System.out.println("Nuevo contenido es:"+cont);
				}

				if (cont.contains("soy:")) { 				
					String[] partes = cont.split(":");
					int idLimite = Integer.parseInt(partes[1]);
					if (idLimite >= id) {
						id = idLimite + 1;
					}
				}
			}
		} catch (SocketTimeoutException e) {
			if(id==0){
				id=1;
			}
			identificado = true;
			mSocket.setSoTimeout(0);
			System.out.println("Mi id es:"+id);
		}
	}

	private void responderSaludo() {
		
		System.out.println("Mensaje respondido: Soy:"+id);

		enviar(serialize(new MensajeID("soy:"+id)), GROUP_ADDRESS, 5000);
	}

	public void enviar(byte[] buffer, String direccionIP, int pt) {
		try {
			InetAddress hosting = InetAddress.getByName(direccionIP);
			DatagramPacket dPacket = new DatagramPacket(buffer, buffer.length, hosting, pt);
			System.out.println("Se envio data a: " + direccionIP);
			mSocket.send(dPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DatagramPacket recibir() throws IOException {
		byte[] buffer = new byte[1024];
		DatagramPacket dPacket = new DatagramPacket(buffer, buffer.length);
		mSocket.receive(dPacket);
		return dPacket;
	}

	public byte[] serialize(Object o) {
		byte[] info = null;
		try {
			ByteArrayOutputStream baOut = new ByteArrayOutputStream();
			ObjectOutputStream oOut = new ObjectOutputStream(baOut);
			oOut.writeObject(o);
			info = baOut.toByteArray();

			oOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}

	public Object deserialize(byte[] b) {
		Object data = null;
		try {
			ByteArrayInputStream baOut = new ByteArrayInputStream(b);
			ObjectInputStream oOut = new ObjectInputStream(baOut);
			data = oOut.readObject();

			oOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void run() {
		while (life) {
			if (mSocket != null) {
				
				DatagramPacket dPacket;
				try {
					dPacket = recibir();

					if (dPacket != null) {
						Object objRecibido = deserialize(dPacket.getData());
						if (objRecibido != null) {
							if (objRecibido instanceof MensajeID) {
								MensajeID mID = (MensajeID) objRecibido;
								String contenido = mID.getContenido();
								
								if (contenido.contains("nuevo")) {
									responderSaludo();
								}
							}

							setChanged();
							notifyObservers(objRecibido);
							clearChanged();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}

	public int getId() {
		return id;
	}
}
