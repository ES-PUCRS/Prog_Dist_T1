import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceServer extends Remote {
	public int Query  (String query, int port)	throws RemoteException;
	public int Insert (int id 	   , int port)	throws RemoteException;
	public int Remove (int id 	   , int port)	throws RemoteException;
}