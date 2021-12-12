package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.SwingUtilities;

import datastructures.PushPacketHandler;
import packets.request.RequestPacket;
import packets.response.ResponsePacket;

/**
 * Runs the client for the Learning Management System
 * <p>
 * Sends information to the server about what the changes the client has made and requests information the client needs as well as receiving information from the server.
 *
 * @author Daniel Geva, Isaac Fleetwood
 *
 * @version December 10, 2021
 *
 */

public class NetworkManagerClient {
    
	LearningManagementSystemClient lmsc;
    HashMap<RequestPacket, ResponsePacketHandler> packetQueue;
    
    public static final boolean DEBUG_ENABLED = true;
    
    final NameSetter nameSetter;
    final Object connectionSuccessLock = new Object();

    Socket socket;

    Thread inputThread;
    Thread outputThread;
    
    Queue<ResponsePacketHandler> queue = new LinkedList<>();
    ArrayList<PushPacketHandler> pushPacketHandlers = new ArrayList<>();

    public NetworkManagerClient (LearningManagementSystemClient lmsc) {
        this.lmsc = lmsc;
        this.packetQueue = new HashMap<>();
        this.nameSetter = new NameSetter();
    }

    ///Connects to the server and initializes the two threads that send and receive objects
    public void init() {
        this.outputThread = new Thread(new Runnable() {
            @Override
            public void run() {
            	ObjectOutputStream oos = null;
            	boolean success = false;
                do {
                    try {
                    	if(DEBUG_ENABLED) 
                    		nameSetter.setName("127.0.0.1");
                    	else {
	                        synchronized (nameSetter) {
	                            nameSetter.wait();
	                        }
                    	}
                        socket = new Socket(nameSetter.getName(), 4040);
                        
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        
                        success = true;
                        synchronized(connectionSuccessLock) {
                        	connectionSuccessLock.notifyAll();
                        }
                        SwingUtilities.invokeLater(nameSetter.getSuccessRunnable());
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(nameSetter.getErrorRunnable());
                    } catch (InterruptedException e) {
                        return;
                    }
                } while (!success);

                while (true) {
                	synchronized(packetQueue) {
                		try {
							if(packetQueue.size() == 0)
								packetQueue.wait();
						} catch (InterruptedException e) {
							return;
						}
                	}
                    while (packetQueue.size() != 0) {
                        try {
                            RequestPacket request = packetQueue.keySet().iterator().next();
                            queue.add(packetQueue.get(request));
                            oos.writeObject(request);
                            packetQueue.remove(request);
                        } catch (IOException e) {
                            return;
                        }
                    }
                }

            }
        });

        this.inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
            	ObjectInputStream ois = null;
                boolean success = false;
                do {
                    try {
                        synchronized(connectionSuccessLock) {
                        	connectionSuccessLock.wait();
                        }
                        ois = new ObjectInputStream(socket.getInputStream());
                        success = true;
                    } catch (IOException e) {
                        nameSetter.getErrorRunnable().run();
                    } catch(InterruptedException e) {
                    	return;
                    }
                } while (!success);
                while (true) {
                    try {
                    	Object responseObj = ois.readObject();
                        if (!(responseObj instanceof ResponsePacket)) {
                            continue;
                        }
                        ResponsePacket response = (ResponsePacket) responseObj;
                        if (!response.getPush()) {
                            ResponsePacketHandler handler = queue.remove();
                            SwingUtilities.invokeLater(() -> {
                                handler.handlePacket(response);
                            });
                        } else {
                            for (PushPacketHandler handler : pushPacketHandlers) {
                                if (handler.canHandle(response)) {
                                	SwingUtilities.invokeLater(() -> {
                                        handler.handlePacket(response);
                                    });
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                    	return;
                    }
                }
            }
        });

        this.inputThread.start();
        this.outputThread.start();
    }

    ///Adds the push packet handler from the list
    public void addPushHandler(PushPacketHandler pPHandler) {
        pushPacketHandlers.add(pPHandler);
    }

    ///Removes a push packet handler from the list
    public void removePushHandler(PushPacketHandler pPHandler) {
        pushPacketHandlers.remove(pPHandler);
    }

    ///Closes the threads upon exit
    public void exit() {
    	this.outputThread.interrupt();
    	this.inputThread.interrupt();
    }

    ///Method used by the UIManger in order to add a packet to the packetQueue to be sent
    public ResponsePacketHandler sendPacket(RequestPacket requestPacket) {
        ResponsePacketHandler handler = new ResponsePacketHandler();
        this.packetQueue.put(requestPacket, handler);
    	synchronized(packetQueue) {
    		packetQueue.notifyAll();
    	}
        return handler;
    }

    ///A class used to set the IP address and deal with errors
    class NameSetter {
        String name = "";
        Runnable errorRunnable = () -> {};
        Runnable successRunnable = () -> {};

        public void setErrorRunnable(Runnable errorRunnable) {
            this.errorRunnable = errorRunnable;
        }

        public Runnable getErrorRunnable() {
            return errorRunnable;
        }

        public Runnable getSuccessRunnable() {
			return successRunnable;
		}

		public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

		public void setSuccessRunnable(Runnable runnable) {
			this.successRunnable = runnable;
		}
    }


}
