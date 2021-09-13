import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceSemaphore extends Remote {
	public int response(String content, String port) throws RemoteException;
	public int register(String ip, String port) 	 throws RemoteException;
}