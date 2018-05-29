
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
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Coordinator extends UnicastRemoteObject implements CoordinatorInterface {

    final int IDsAmount = 5;

    public String WritePermission;
    public boolean MasterCoordinator;
    public Integer MyID;
    public Integer[] IDs;
    public ReentrantLock lock;

    public long AllocationTime;

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

    // REQUISITA ACESSO AO RECURSO
    @Override
    public boolean RequestResource(String Requester) throws RemoteException {

        if (WritePermission.equals("")) {
            WritePermission = Requester;
            AllocationTime = System.currentTimeMillis();
            System.out.println("Granted resource to " + Requester);
            //lock.unlock();
            return true;
        }

        return false;

    }

    // REALIZA AS 10 OPERACOES MATEMATICAS
    // CADA PROCESSO REALIZA UM PADRAO DE OPERACOES
    // UTILIZANDO A CLASSE RANDOM E SEU ID COMO SEED
    @Override
    public String MathOperation() throws FileNotFoundException, IOException {

        String mathResult = "";
        Random rand = new Random();
        rand.setSeed(MyID);
        int[] randoms = new int[10];

        String debugLog = "";
        // DEFINE AS DEZ OPERACOES A SEREM REALIZADAS
        // CADA ID REALIZA SEMPRE AS MESMAS 10 OPERACOES
        for (int i = 0; i < 10; i++) {
            randoms[i] = rand.nextInt(5) + 1;
            debugLog += randoms[i] + ", ";
        }
        System.out.println(debugLog);

        
        // DEZ OPERACOES NO ARQUIVO
        for (int i = 0; i < 10; i++) {

            lock.lock();

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
                String operation = "";

                switch (randoms[i]) {   //MyID
                    case 1:
                        operation = " + 20";
                        lastLineInteger += 20;
                        break;

                    case 2:
                        operation = " * 2";
                        lastLineInteger *= 2;
                        break;

                    case 3:
                        operation = " - 25";
                        lastLineInteger -= 25;
                        break;

                    case 4:
                        operation = " * 3";
                        lastLineInteger *= 3;
                        break;

                    case 5:
                        operation = " / 2";
                        lastLineInteger /= 2;
                        break;

                }

                BW.write(operation + " (Worker" + MyID + ")");
                BW.newLine();
                BW.write(String.valueOf(lastLineInteger));
                System.out.println(operation);

                BW.close();

            } finally {
                lock.unlock();
            }

        }

        return mathResult;

    }

    // LIBERTA O RECURSO REQUISITADO
    @Override
    public boolean ReleaseResource(String Requester) {

        lock.lock();
        try {
            if (Requester.equals(WritePermission)) {
                WritePermission = "";
                System.out.println(Requester + " has released");

                return true;
            } else {
                //System.out.println("Released messed up ? " + Requester + " " + WritePermission + "Equals ? " + Requester.equals(WritePermission));
            }
        } finally {
            lock.unlock();
        }

        return false;

    }

    public boolean Alive() throws RemoteException {
        return true;
    }

    // REGISTRO DE ID
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

    
    // VOTO PARA ELEICAO
    public boolean MayICoordinateNow(int RequesterID) throws RemoteException {
        return (!MasterCoordinator && RequesterID > MyID);
    }

    // WRITE PERMISSION TIMEOUT
    public boolean CheckTimeout() {
        long Now = System.currentTimeMillis();
        if ((!WritePermission.equals("")) && Now >= (AllocationTime + 10000)) {
            ReleaseResource(WritePermission);
            return true;
        }
        return false;
    }

}
