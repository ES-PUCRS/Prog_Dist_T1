import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;


class RMIClient extends UnicastRemoteObject implements InterfaceClient {
	private static volatile boolean debug = true;
	private static volatile int i, j;


	public RMIClient() throws RemoteException { }


	public static void main(String[] args) {
		int result;

		if (args.length != 4) {
			System.out.println("Usage: java RMIClient <server ip> <server port> <client ip> <client port>");
			System.exit(1);
		}
	

		try {
			System.setProperty("java.rmi.server.hostname", args[2]);
			LocateRegistry.createRegistry(Integer.parseInt(args[3]));
			System.out.println("java RMI registry created.");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}


		try {
			String client = "rmi://" + args[2] + ":" + args[3] + "/callback";
			Naming.rebind(client, new RMIClient());
			System.out.println("RMI Server is ready.");
		} catch (Exception e) {
			System.out.println("RMI Server failed: " + e.getLocalizedMessage());
		}

		String remoteHostName = args[0];
		String connectLocation = "rmi://" + remoteHostName + ":" + args[1] + "/Server";

		InterfaceServer remoteConnection = null;
		try {
			System.out.println("Connecting to server at : " + connectLocation);
			remoteConnection = (InterfaceServer) Naming.lookup(connectLocation);
		} catch (Exception e) {
			System.out.println ("RMIClient failed: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}

		while (true) {
			try {
				remoteConnection.Query("This is a query that was: ", Integer.parseInt(args[3]));
				System.out.println("Call to server..." );
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {}
		}
	}


	public int Callback(String callback) {
		System.out.println("Called back, result is: " + callback);
		return 1;
	}
}