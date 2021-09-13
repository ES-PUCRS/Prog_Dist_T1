import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.InputMismatchException;


class RMIClient extends UnicastRemoteObject implements InterfaceCallback {
	private static volatile boolean debug = true;
	private static volatile int query, insert, remove;

	private static volatile InterfaceServer remoteConnection;


	public RMIClient() throws RemoteException { }


	public static void main(String[] args) {
		int result;

		if (args.length != 7) {
			System.out.println("Usage: java RMIClient <semaphore ip> <semaphore port> <client ip> <client port> <Queries %> <Inserts %> <Deletions %>");
			System.exit(1);
		}
		try {
			query = Integer.parseInt(args[4]);
			insert = Integer.parseInt(args[5]);
			remove = Integer.parseInt(args[6]);
			if((query + insert + remove) != 100)
				throw new InputMismatchException("The % was not correct");
		} catch(Exception e) {
			System.exit(1);
		}

		registryRMI(args[2], args[3]);
		registerHost(args[0], args[1]);
		runtime(args[3]);
	}

/* ------------------------------------------------------------- */

	private static void registryRMI(String ip, String port) {
		int registry = Integer.parseInt(port);

		try {
			System.setProperty("java.rmi.server.hostname", ip);

			LocateRegistry.createRegistry(registry);
			System.out.println("java RMI registry created.");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}

		try {
			String server = "rmi://" + ip + ":" + registry + "/Callback";
			Naming.rebind(server, new RMIServer());
			System.out.println("RMI Server is ready.");
		} catch (Exception e) {
			System.out.println("RMI Server failed: " + e);
			if(debug)
				e.printStackTrace();
		}
	}
	
	private static void registerHost(String hostname, String port) {
		String connectLocation = "rmi://" + hostname + ":" + port + "/Request";

		remoteConnection = null;
		try {
			System.out.println("Connecting to semaphore at : " + connectLocation);
			remoteConnection = (InterfaceServer) Naming.lookup(connectLocation);
		} catch (Exception e) {
			System.out.println ("RMI connection failed: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}
	}

	private static void runtime(String port) {
		while (true) {
			try {
				remoteConnection.Query("This is a query that was: ", port);
				System.out.println("Call to server..." );
			} catch (RemoteException e) {
				System.out.println(e.getLocalizedMessage());
				if(debug)
					e.printStackTrace();
				
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {}
		}
	}

/* InterfaceCallback ------------------------------------------- */

	@Override
	public int Callback(String callback) {
		System.out.println("Called back, result is: " + callback);
		return 1;
	}
}