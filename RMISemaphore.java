import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.HashMap;

import java.lang.InterruptedException;

class RMISemaphore extends UnicastRemoteObject implements InterfaceSemaphore, InterfaceServer {
	private static volatile boolean debug = true;
	private static volatile int registry;

	private static volatile String remoteHostName, remoteHostPort;
	private static volatile String callback;
	private static volatile boolean changed;

	private static volatile HashMap<String, Integer> serverList;
	private static volatile HashMap<String, String>  busyServer;
	private static volatile LinkedList<String>  	 freeServer;

	private static Semaphore mutex;
	private static String method;
	private static int count;


	public RMISemaphore() throws RemoteException {
		serverList = new HashMap<String, Integer>();
		busyServer = new HashMap<String, String>();
		freeServer = new LinkedList<String>();
		
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

	private static void runtime() {
		while (true) {
			if (changed == true) {
				changed = false;

				String connectLocation = "rmi://" + remoteHostName + ":" + remoteHostPort + "/Callback";
				InterfaceCallback callbackLocation = null;
				try {
					System.out.println(callback + " >> " + connectLocation);
					callbackLocation = (InterfaceCallback) Naming.lookup(connectLocation);
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

/* MUTEX ------------------------------------------------------- */

	public static void p() throws InterruptedException {
		mutex.acquire();
		count += 1;
	}

	public static void v(){
		mutex.release();
	}

/* InterfaceSemaphore ------------------------------------------ */
	
	@Override
	public int response(String content, String port) {
		System.out.println("Received: " + content);
		String dst = "";
		try {
			dst = busyServer.remove(key(getClientHost(), port));
		} catch (Exception e) {

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
		serverList.put(ip, Integer.parseInt(port));
		freeServer.add(key(ip, port));
		remoteHostPort = port;
		remoteHostName = ip;

		changed = true;
		return 1;
	}


/* InterfaceServer --------------------------------------------- */

	@Override
	public int Query(String query, String port) {
		String dst = "";
		try {
			dst = key(getClientHost(), port);
		} catch (Exception e) {}

		try { p(); }
		catch (Exception e) {}
		
		System.out.println(dst + " reachs query");
		return 1;
	}

	@Override
	public int Insert(int id, String port) {
		try { p(); }
		catch (Exception e) {}

		return 1;
	}

	@Override
	public int Remove(int id, String port) {
		try { p(); }
		catch (Exception e) {}

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