import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Arrays;

import java.lang.InterruptedException;

class RMISemaphore extends UnicastRemoteObject implements InterfaceSemaphore, InterfaceServer {
	private static volatile boolean debug = true;
	private static volatile int registry;

	private static volatile String remoteHostName, remoteHostPort;
	private static volatile String callback;
	private static volatile boolean changed;

	private static volatile HashMap<String, String>  busyServers;
	private static volatile LinkedList<String>  	 freeServers;


	private static volatile Semaphore serversMutex;
	private static volatile int countAvailable;


	private static volatile Semaphore mutex;
	private static volatile String method;
	private static volatile int count;


	public RMISemaphore() throws RemoteException {
		busyServers = new HashMap<String, String>();
		freeServers = new LinkedList<String>();
		
		serversMutex  = new Semaphore(1);
		try { serversMutex.acquire(); }
		catch (Exception e) {}
		countAvailable  = 0;

		mutex  = new Semaphore(1);
		method = "";
		count  = 0;
	}


	public static void main(String[] args) throws RemoteException {
		if (args.length != 2) {
			System.out.println("Usage: java RMISemaphore <semaphore ip> <semaphore port>");
			System.exit(1);
		}
		registryRMI(args[0], args[1]);
		runtime();
	}



/* ------------------------------------------------------------- */

	private static void registryRMI(String ip, String port) {
		registry = Integer.parseInt(port);

		try {
			System.setProperty("java.rmi.server.hostname", ip);

			LocateRegistry.createRegistry(registry);
			System.out.println("RMI SEMAPHORE registry created.");
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}

		try {
			String server = "rmi://" + ip + ":" + registry + "/Registry";
			String client = "rmi://" + ip + ":" + registry + "/Request";
			Naming.rebind(server, new RMISemaphore());
			Naming.rebind(client, new RMISemaphore());
			System.out.println("RMI Semaphore is ready.");
		} catch (Exception e) {
			System.out.println("RMI Semaphore failed: " + e);
			if(debug)
				e.printStackTrace();
		}
	}

	private static InterfaceServer registerHost(String hostname, String port) {
		String connectLocation = "rmi://" + hostname + ":" + port + "/Server";

		InterfaceServer remoteConnection = null;
		try {
			remoteConnection = (InterfaceServer) Naming.lookup(connectLocation);
		} catch (Exception e) {
			System.out.println ("RMI connection failed: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}

		return remoteConnection;
	}

	private static void runtime() {
		while (true) {
			if (changed == true) {
				changed = false;

				String connectLocation = "rmi://" + remoteHostName + ":" + remoteHostPort + "/Callback";
				InterfaceCallback callbackLocation = null;
				try {
					System.out.println(callback + " > " + connectLocation);
					callbackLocation = (InterfaceCallback) Naming.lookup(connectLocation);
				} catch (Exception e) {
					System.out.println ("Callback connection failed: " + e.getLocalizedMessage());
					if(debug)
						e.printStackTrace();
				}
				try {
					callbackLocation.Callback(callback);
					v();
				} catch (RemoteException e) {
					if(debug)
						e.printStackTrace();
				}
			}
			
			try { Thread.sleep(100); }
			catch (InterruptedException ex) {}
		}
	}

/* MUTEX ------------------------------------------------------- */

	public static void p() throws InterruptedException {
		mutex.acquire();
		count++;
	}

	public static void v(){
		count--;
		if(count == 0){
			mutex.release();
			method = "";
		}
	}


	public static void pServer() throws InterruptedException {
		if(countAvailable == 0) {
			System.out.println("** Waiting free server");
			serversMutex.acquire();
		} else
			countAvailable--;
	}

	public static void vServer(){
		countAvailable++;
		if(countAvailable > 0){
			serversMutex.release();
		}
	}

/* InterfaceSemaphore ------------------------------------------ */
	
	@Override
	public int response(String content, String port) {
		String dst = "";
		try {
			String serverKey = key(getClientHost(), port);
			dst = busyServers.remove(serverKey);
			freeServers.add(serverKey);
	
			System.out.println(content + " < Server::" + serverKey);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		

		remoteHostPort = getPort(dst);
		remoteHostName = getIp(dst);
		callback = content;

		changed = true;

		v();
		return 1;
	}

	@Override
	public int register(String ip, String port) {
		callback = "Server registered as key <" + ip + ":" + port + ">";
		freeServers.add(key(ip, port));
		remoteHostPort = port;
		remoteHostName = ip;
		changed = true;

		vServer();
		return 1;
	}


/* InterfaceServer --------------------------------------------- */

	@Override
	public int Query(String query, String port) {
		InterfaceServer remoteConnection = null;
		try {
			pServer();
			String server = freeServers.remove();
			busyServers.put(server, key(getClientHost(), port));
			remoteConnection = (InterfaceServer) registerHost(getIp(server),getPort(server));

			System.out.println("JOB Query to: " + server);
		} catch (Exception e) {}

		try {
			p();
			remoteConnection.Query(query, port);
		} catch (Exception e) {
			System.out.println("RMI ERROR CALLING SERVER: " + e.getLocalizedMessage());
			if(debug)
				e.printStackTrace();
		}

		return 1;
	}

	@Override
	public int Insert(int id, String port) {
		return 1;
	}

	@Override
	public int Remove(int id, String port) {
		return 1;
	}


/* AUX Methods ------------------------------------------------- */

	private String getIp(String key) {
		return key.split(":")[0];
	}

	private String getPort(String key) {
		return key.split(":")[1];
	}

	private String key(String ip, String port) {
		return ip+":"+port;
	}

}