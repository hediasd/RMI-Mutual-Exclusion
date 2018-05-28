import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface remota
public interface CoordinatorInterface extends Remote {
    
    
	public boolean RequestResource(String Requester) throws RemoteException;
        public boolean ReleaseResource(String Requester) throws RemoteException;
        public boolean Alive() throws RemoteException;
        public Integer Register() throws RemoteException;
        public boolean MayICoordinateNow (int RequesterID) throws RemoteException;

        

}

