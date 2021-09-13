import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.concurrent.Semaphore;

import java.lang.InterruptedException;

class RMIServer extends UnicastRemoteObject implements InterfaceServer, InterfaceCallback {
	private static volatile boolean debug = false;
	private static volatile int registry;

	private static volatile String remoteHostName, remoteHostPort;
	private static volatile boolean changed;
	private static volatile String response;
	private static volatile String method;

	private static volatile InterfaceSemaphore remoteConnection;


	public RMIServer() throws RemoteException {}


	public static void main(String[] args) throws RemoteException {
		if (args.length != 4) {
			System.out.println("Usage: java RMIServer <semaphore ip> <semaphore port> <server ip> <server_port>");
			System.exit(1);
		}
		registryRMI(args[2], args[3]);
		registerHost(args[0], args[1]);
		runtime(args[2], args[3]);
	}



/* ------------------------------------------------------------- */

	private static void registryRMI(String ip, String port) {
		registry = Integer.parseInt(port);

		try {
			System.setProperty("java.rmi.server.hostname", ip);

			LocateRegistry.createRegistry(registry);
			System.out.println("RMI registry created at <" +ip+":"+port+">");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
			System.exit(1);
		}

		try {
			String server = "rmi://" + ip + ":" + registry + "/Server";
			String callback = "rmi://" + ip + ":" + registry + "/Callback";
			Naming.rebind(server, new RMIServer());
			Naming.rebind(callback, new RMIServer());
			System.out.println("RMI Server is ready.");
		} catch (Exception e) {
			System.out.println("RMI Server failed: " + e);
			if(debug)
				e.printStackTrace();
		}
	}

	private static void registerHost(String hostname, String port) {
		String connectLocation = "rmi://" + hostname + ":" + port + "/Registry";

		remoteConnection = null;
		try {
			System.out.println("Connecting to semaphore at: " + connectLocation);
			remoteConnection = (InterfaceSemaphore) Naming.lookup(connectLocation);
		} catch (Exception e) {
			System.out.println ("RMI connection failed: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}
	}

	private static void runtime(String ip, String port) {
		try {
			remoteConnection.register(ip, port);
			System.out.println("> Registering server on semaphore..." );
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		while (true) {
			if (changed == true) {
				changed = false;

				try {
					remoteConnection.response(response, method, port);
				} catch (RemoteException e) {
					System.out.println(e.getLocalizedMessage());
					if(debug)
						e.printStackTrace();
				}
			}
			
			try { Thread.sleep(2000); }
			catch (InterruptedException ex) {}
		}
	}

/* InterfaceSemaphore ------------------------------------------ */

	@Override
	public int Callback(String callback) {
		System.out.println("Received: " + callback);
		return 1;
	}

/* InterfaceServer --------------------------------------------- */

	@Override
	public int Query(String query, String port) {
		response = query + " QUERIADO!";
		changed = true;
		method = "query";

		try {
			remoteHostName = getClientHost();
			remoteHostPort = port;
			System.out.println("Quering: \'" + query + "\' From: " +remoteHostName+":"+port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}

	@Override
	public int Insert(int id, String port) {
		response = id + " INSERTADO!";
		changed = true;
		method = "insert";

		try {
			remoteHostName = getClientHost();
			remoteHostPort = port;
			System.out.println("Inserting id: \'" + id + "\' From: " + remoteHostName+":"+port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}

	@Override
	public int Remove(int id, String port) {
		response = id + " REMOVADO!";
		changed = true;
		method = "remove";

		try {
			remoteHostName = getClientHost();
			remoteHostPort = port;
			System.out.println("Removing id: \'" + id + "\' From: " +remoteHostName+":"+port);
		} catch (Exception e) {
			System.out.println ("Failed to get client IP");
			e.printStackTrace();
		}
		return 1;
	}
}