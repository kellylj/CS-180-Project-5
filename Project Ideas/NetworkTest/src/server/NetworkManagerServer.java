package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import packets.request.ExampleRequestPacket;
import packets.response.ExampleResponsePacket;

public class NetworkManagerServer {

	MainServer mainServer;
	
	public NetworkManagerServer(MainServer mainServer) {
		this.mainServer = mainServer;
	}
	
	public void init() {
		try {
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(8080);
			
			while(true) {
				final Socket socket = ss.accept();
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						while(true) {
							try {
								Object obj = ois.readObject();
								if(!(obj instanceof ExampleRequestPacket)) {
									System.out.println("Error. Non-RequestPacket sent through stream.");
									System.out.println("Not responding to that packet.");
									continue;
								}
								ExampleRequestPacket requestPacket = (ExampleRequestPacket) obj;
								ExampleResponsePacket response = requestPacket.serverHandle(mainServer);
								
								oos.writeObject(response);
							} catch (EOFException e) {
								// Client disconnected.
								return;
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								continue;
							} catch (IOException e) {
								e.printStackTrace();
								continue;
							}
							
						}
					}
					
				});
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
