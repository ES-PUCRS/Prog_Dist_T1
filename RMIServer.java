import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.concurrent.Semaphore;

class RMIServer extends UnicastRemoteObject implements InterfaceServer {
	private static volatile boolean debug = true;
	private static volatile int registry;

	private static volatile boolean changed;
	private static volatile String callback;
	private static volatile String remoteHostName, remoteHostPort;


	public RMIServer() throws RemoteException { }


	public static void main(String[] args) throws RemoteException {
		if (args.length != 2) {
			System.out.println("Usage: java RMIServer <server ip> <server_port>");
			System.exit(1);
		}
		registry = Integer.parseInt(args[1]);

		try {
			System.setProperty("java.rmi.server.hostname", args[0]);

			LocateRegistry.createRegistry(registry);
			System.out.println("java RMI registry created.");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}

		try {
			String server = "rmi://" + args[0] + ":" + registry + "/Server";
			Naming.rebind(server, new RMIServer());
			System.out.println("RMI Server is ready.");
		} catch (Exception e) {
			System.out.println("RMI Server failed: " + e);
			if(debug)
				e.printStackTrace();
		}

		
		while (true) {
			if (changed == true) {
				changed = false;

				String connectLocation = "rmi://" + remoteHostName + ":" + remoteHostPort + "/callback";

				InterfaceClient callbackLocation = null;
				try {
					System.out.println("Calling client back at : " + connectLocation);
					callbackLocation = (InterfaceClient) Naming.lookup(connectLocation);
				} catch (Exception e) {
					System.out.println ("Callback connection failed: " + e.getLocalizedMessage());
					if(debug)
						e.printStackTrace();
				}

				try {
					callbackLocation.Callback(callback);
				} catch (RemoteException e) {
					if(debug)
						e.printStackTrace();
				}
			}
			
			try { Thread.sleep(100); }
			catch (InterruptedException ex) {}
		}
	}



	public int Query(String query, int port) {
		callback = query + " QUERIADO!";
		changed = true;
		try {
			remoteHostName = getClientHost();
			remoteHostPort = Integer.toString(port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}

	public int Insert(int id, int port) {
		callback = id + " INSERTADO!";
		changed = true;
		try {
			remoteHostName = getClientHost();
			remoteHostPort = Integer.toString(port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}

	public int Remove(int id, int port) {
		callback = id + " REMOVADO!";
		changed = true;
		try {
			remoteHostName = getClientHost();
			remoteHostPort = Integer.toString(port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}
}