package intentoAutoID;

import java.io.*;
import java.net.*;
import java.util.Observable;

public class Comunicacion extends Observable implements Runnable {

	public MulticastSocket mSocket;
	private final int PORT = 5000;
	private final String GROUP_ADDRESS = "226.24.6.7";
	private boolean life = true;
	private boolean identificado;
	private int id;

	public Comunicacion() {

		try {
			mSocket = new MulticastSocket(PORT);
			InetAddress host = InetAddress.getByName(GROUP_ADDRESS);
			mSocket.joinGroup(host);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			autoID();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void autoID() throws IOException {
		try {
			byte[] data = serialize(new MensajeID("Hola soy nuevo"));
			enviar(data, GROUP_ADDRESS, PORT);
			mSocket.setSoTimeout(500);
			while (!identificado) {
				DatagramPacket dPacket = recibir();
				if (dPacket != null) {
					MensajeID msg = (MensajeID) deserialize(dPacket.getData());
					String contenido = msg.getContenido();

					if (contenido.contains("soy:")) {
						String[] division = contenido.split(":");
						int idLimite = Integer.parseInt(division[1]);
						if (idLimite >= id) {
							id = idLimite + 1;
						}
					}
				}
			}
		} catch (SocketTimeoutException e) {
			if (id == 0) {
				id = 1;
			}
			identificado = true;
			System.out.println("Mi id es:" + id);
			mSocket.setSoTimeout(0);
		}
	}

	public void enviar(byte[] data, String ipAdrs, int puerto) throws IOException {
		InetAddress host = InetAddress.getByName(ipAdrs);
		DatagramPacket dPacket = new DatagramPacket(data, data.length, host, puerto);

		mSocket.send(dPacket);
	}

	private DatagramPacket recibir() throws IOException {
		byte[] data = new byte[1024];
		DatagramPacket dPacket = new DatagramPacket(data, data.length);
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
				try {
					DatagramPacket dPacket = recibir();
					if (dPacket != null) {
						MensajeID msg = (MensajeID) deserialize(dPacket.getData());
						String contenido = msg.getContenido();

						if (contenido.contains("soy nuevo")) {
							// Responder
							byte[] data = serialize(new MensajeID("soy:" + id));
							enviar(data, GROUP_ADDRESS, PORT);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public int getId() {
		return id;
	}
}
