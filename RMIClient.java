import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.InputMismatchException;
import java.util.Random;

class RMIClient extends UnicastRemoteObject implements InterfaceCallback {
	private static volatile boolean debug = false;
	private static volatile int query, insert, remove;

	private static volatile InterfaceServer remoteConnection;


	public RMIClient() throws RemoteException { }


	public static void main(String[] args) {
		int result;
		if (args.length < 6 || args.length > 7) {
			System.out.println("Usage: java RMIClient <semaphore ip> <semaphore port> <client ip> <client port> <Queries %> <Inserts %> [method]");
			System.exit(1);
		}
		try {
			query = Integer.parseInt(args[4]);
			insert = Integer.parseInt(args[5]);
			if((query + insert) > 99)
				throw new InputMismatchException("The % does not match");

			System.out.println("Queries: " + query + "%, Insertions: " + insert + "%, Deletions: " + (100-(query+insert)) + "%");
		} catch(Exception e) {
			System.out.println("Error: " + e.getLocalizedMessage());
			System.exit(1);
		}

		String method = "";
		if (args.length == 7){
			method = args[6];
		}

		registryRMI(args[2], args[3]);
		registerHost(args[0], args[1]);
		runtime(args[3], method);
	}

/* ------------------------------------------------------------- */

	private static void registryRMI(String ip, String port) {
		int registry = Integer.parseInt(port);

		try {
			System.setProperty("java.rmi.server.hostname", ip);

			LocateRegistry.createRegistry(registry);
			System.out.println("RMI CLIENT registry created.");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}

		try {
			String server = "rmi://" + ip + ":" + registry + "/Callback";
			Naming.rebind(server, new RMIClient());
			System.out.println("RMI Client is ready.");
		} catch (Exception e) {
			System.out.println("RMI Client failed: " + e);
			if(debug)
				e.printStackTrace();
		}
	}
	
	private static void registerHost(String hostname, String port) {
		String connectLocation = "rmi://" + hostname + ":" + port + "/Request";

		remoteConnection = null;
		try {
			System.out.println("Bridge: " + connectLocation);
			remoteConnection = (InterfaceServer) Naming.lookup(connectLocation);
		} catch (Exception e) {
			System.out.println ("RMI connection failed: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}
	}

	private static void runtime(String port, String method) {
		Random random = new Random();
		int chance = 0;

		while (true) {
			if(method.equals("")) {
				chance = random.nextInt(100);
				if (chance < query) {
					method = "query";
				} else if ( chance < (insert+query) ) {
					method = "insert";
				} else {
					method = "remove";
				}	
			}
			
			try {
				int id = random.nextInt(100);
				switch(method.toLowerCase()) {
					case "query":
						System.out.println("Call to server QUERY " + id);
						remoteConnection.Query(method + ": " + id, port);
						break;
					case "insert":
						System.out.println("Call to server INSERT " + id);
						remoteConnection.Insert(id, port);
						break;
					case "remove":
						System.out.println("Call to server REMOVE " + id);
						remoteConnection.Remove(id, port);
						break;

					default:
						System.out.println("Call to server QUERY" );
						remoteConnection.Query("- TEST QUERY -", port);
				}

			} catch (RemoteException e) {
				System.out.println(e.getLocalizedMessage());
				if(debug)
					e.printStackTrace();

				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {}

			method = "";
			// break;
		}
	}

/* InterfaceCallback ------------------------------------------- */

	@Override
	public int Callback(String callback) {
		System.out.println("Returned: " + callback);
		return 1;
	}
}