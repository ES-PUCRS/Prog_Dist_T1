import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceCallback extends Remote {
	public int Callback(String callback) throws RemoteException;
}