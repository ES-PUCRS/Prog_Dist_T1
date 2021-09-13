import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceClient extends Remote {
	public int Callback(String val) throws RemoteException;
}