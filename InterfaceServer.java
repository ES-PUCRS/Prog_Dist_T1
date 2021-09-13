import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceServer extends Remote {
	public int Query  (String query, String port)	throws RemoteException;
	public int Insert (int id 	   , String port)	throws RemoteException;
	public int Remove (int id 	   , String port)	throws RemoteException;
}