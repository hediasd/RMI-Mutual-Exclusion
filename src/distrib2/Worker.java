
import java.io.BufferedReader;
import java.io.FileReader;
import static java.lang.Thread.sleep;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.util.resources.cldr.zh.LocaleNames_zh_Hant_HK;

class Worker {

    static Coordinator LocalCoordinator;
    static boolean Bound = false;
    static Random rand = new Random();

    public static void main(String[] argv) {

        try {

            // Start
            try {
                java.rmi.registry.LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                System.out.println("RMI registry already running.");
            }

            LocalCoordinator = new Coordinator(false);
            WorkerRegister();

        } catch (NotBoundException e) {

            System.out.println("No server, so I trigger elections");
            LocalCoordinator.MasterCoordinator = Elections();

        } catch (Exception e) {
            System.out.println("I have failed during startup");
            e.printStackTrace();
        }

        while (true) {
            //SE SERVIDOR
            if (LocalCoordinator.MasterCoordinator) {
                //TIMEOUT DE PROCESSOS MORTOS
                LocalCoordinator.CheckTimeout();

                System.out.println("... Serving ...");
                
            //SE WORKER
            } else {

                boolean Elect = false;

                try {
                    // CONSTANTEMENTE REQUERE RECURSOS
                    CoordinatorInterface Server = (CoordinatorInterface) Naming.lookup("//localhost/Server");
                    boolean Allowed = Server.RequestResource("Worker" + LocalCoordinator.MyID);
                    
                    System.out.println("Resource granted ? " + Allowed);
                    
                    if (Allowed) {
                        
                        // REALIZA OPERACOES E DORME
                        LocalCoordinator.MathOperation();
                        boolean Released = Server.ReleaseResource("Worker" + LocalCoordinator.MyID);
                        System.out.println("Resource released ? " + Released);
                        sleep(2000 + rand.nextInt(1000));
                        
                    }

                } catch (NotBoundException | ConnectException f) {
                    // elections
                    try {
                        java.rmi.registry.LocateRegistry.createRegistry(1099);
                    } catch (RemoteException r) {
                        System.out.println("RMI registry already running.");
                    }
                    System.out.println("No server, so I trigger elections");
                    Elect = true;

                } catch (Exception e) {

                    if (e instanceof ConnectException) {
                        System.out.println("ConnectException");

                    } else {
                        System.out.println("Some exception");
                        e.printStackTrace();
                    }

                }

                if (Elect) {
                    LocalCoordinator.MasterCoordinator = Elections();
                }

            }

            try {
                sleep(500);
            } catch (InterruptedException ex) {

            }

        }
    }

    // REGISTRA UM NOVO PROCESSO
    public static void WorkerRegister() throws RemoteException, NotBoundException, MalformedURLException {

        CoordinatorInterface Server = (CoordinatorInterface) Naming.lookup("//localhost/Server");
        if (Server.Alive()) {
            System.out.println("Coordinator is alive");
        }

        LocalCoordinator.MyID = Server.Register();
        System.out.println("My new ID is " + LocalCoordinator.MyID);
        if (LocalCoordinator.MyID >= 0) {
            Naming.rebind("Worker" + LocalCoordinator.MyID, LocalCoordinator);
            System.out.println("I'm bound as Worker" + LocalCoordinator.MyID);
            Bound = true;
        } else {
            System.out.println("Something messed up the registering process !!!");
        }
    }

    
    // REALIZA ELEICOES ENTRE OS PROCESSOS
    public static boolean Elections() {

        boolean Elected = true;

        for (int i = 0; i < 5; i++) {

            if (i == LocalCoordinator.MyID) {
                continue;
            }

            try {

                System.out.println("Asking Worker" + i + " if I may become server");

                CoordinatorInterface OtherWorker = (CoordinatorInterface) Naming.lookup("//localhost/Worker" + i);
                boolean OtherWorkerAlive = OtherWorker.Alive();

                Elected = OtherWorker.MayICoordinateNow(LocalCoordinator.MyID);

                if (!Elected) {
                    System.out.println("Worker" + i + " didn't approve");
                    break;
                }
                System.out.println("Worker" + i + " approved");

            } catch (RemoteException f) {
                System.out.println("Remote Exception");
                f.printStackTrace();
            } catch (NotBoundException f) {
                // No such id;
            } catch (Exception f) {

                System.out.println("I have failed somewhere during elections");
                f.printStackTrace();
                Elected = false;

            }

        }

        if (Elected) {

            System.out.println("Ok, no one denied me");

            try {

                //if(Bound) Naming.unbind("Worker"+LocalCoordinator.MyID);
                LocalCoordinator.MasterCoordinator = true;
                //if (LocalCoordinator.MyID >= 0) {
                Naming.rebind("Server", LocalCoordinator);
                //}

                System.out.println("I was elected server !");
                return true;

            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        System.out.println("I was not elected :(");

        return false;
    }

}
