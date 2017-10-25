package NameServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {

        ShutdownAgent nameServer = null;
        try {
            nameServer = new ShutdownAgent();
            nameServer.init();

            System.out.println("Server ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }
}
