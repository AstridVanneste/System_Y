package NameServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main
{
    public static void main(String[] args)
    {
    	/*
        NameServer nameServer = NameServer.getNameServer();
        try
        {
            Registry registry = LocateRegistry.getRegistry(null);
            ShutdownAgentInterface shutDown = (ShutdownAgentInterface) registry.lookup("SHUTDOWNAGENT");
            ResolverInterface resolver = (ResolverInterface) registry.lookup("RESOLVER");
            resolver.lookup(2);
        }
        catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }
		*/
    }
}
