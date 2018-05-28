
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

public class Coordinator extends UnicastRemoteObject implements CoordinatorInterface {

    final int IDsAmount = 5;

    public String WritePermission;
    public boolean MasterCoordinator;
    public Integer MyID;
    public Integer[] IDs;
    public ReentrantLock lock;

    public String LastTime;

    public Coordinator(boolean Master) throws RemoteException {

        lock = new ReentrantLock();

        MyID = -1;
        WritePermission = "";
        MasterCoordinator = Master;
        IDs = new Integer[IDsAmount];

        for (int i = 0; i < IDsAmount; i++) {
            IDs[i] = 0;
        }

    }

    public boolean RequestResource(String Requester) throws RemoteException {

        //lock.lock();
        try {
            if (WritePermission.equals("")) {
                WritePermission = Requester;
                System.out.println("Granted resource to " + Requester);
                //lock.unlock();
                return true;
            }
        } finally {
            //lock.unlock();
        }

        return false;

    }

    public String MathOperation() throws FileNotFoundException, IOException {

        lock.lock();
        String mathResult = "";

        try {
            BufferedReader BR = new BufferedReader(new FileReader("file.txt"));

            String currentLine = "";
            String lastLine = "";
            int lastLineInteger = 1;

            do {
                lastLine = currentLine;
                System.out.println("Line " + lastLine);
            } while ((currentLine = BR.readLine()) != null);
            BR.close();
            BufferedWriter BW = new BufferedWriter(new FileWriter("file.txt", true));

            lastLineInteger = Integer.parseInt(lastLine);
            System.out.println("Last line is " + lastLineInteger);

            System.out.println(lastLineInteger);
            String operation = "";

            switch (MyID) {
                case 1:
                    operation = " + 10";
                    lastLineInteger += 10;
                    BW.write(operation);
                    BW.newLine();
                    BW.write(String.valueOf(lastLineInteger));
                    System.out.println(operation);
                    break;
                case 2:
                    operation = " * 2";
                    lastLineInteger *= 2;
                    BW.write(operation);
                    BW.newLine();
                    BW.write(String.valueOf(lastLineInteger));
                    System.out.println(operation);
                    break;
                case 3:
                    operation = " - 15";
                    lastLineInteger -= 15;
                    BW.write(operation);
                    BW.newLine();
                    BW.write(String.valueOf(lastLineInteger));
                    System.out.println(operation);
                    break;

                case 4:

                    break;

                case 5:

                    break;

                default:
                    operation = " pow 2";
                    lastLineInteger *= lastLineInteger;
                    BW.write(operation);
                    BW.newLine();
                    BW.write(lastLineInteger);
                    System.out.println(operation);
                    break;

            }

            BW.close();

        } finally {
            lock.unlock();
        }

        return mathResult;

    }

    public boolean ReleaseResource(String Requester) {

        lock.lock();
        try {
            if (Requester.equals(WritePermission)) {
                WritePermission = "";
                System.out.println(Requester + " has released");
                
                return true;
            } else {
                System.out.println("Released messed up ? " + Requester + " " + WritePermission + "Equals ? " + Requester.equals(WritePermission));
            }
        } finally {
            lock.unlock();
        }

        return false;

    }

    public boolean Alive() throws RemoteException {
        return true;
    }

    public Integer Register() throws RemoteException {
        for (int i = 1; i < IDsAmount; i++) {
            System.out.println(IDs[i]);
            if (IDs[i] == 0) {
                IDs[i] = 1;
                System.out.println(i);
                return i;
            }
        }
        return -1;
    }

    public boolean MayICoordinateNow(int RequesterID) throws RemoteException {
        return (!MasterCoordinator && RequesterID > MyID);
    }

}
