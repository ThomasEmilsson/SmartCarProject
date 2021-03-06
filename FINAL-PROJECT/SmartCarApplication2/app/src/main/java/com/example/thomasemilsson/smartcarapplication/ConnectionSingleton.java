package com.example.thomasemilsson.smartcarapplication;

/**
 * Created by thomasemilsson on 5/22/16.
 */


/** 
 * Holds an instance of a ConnectionHandler class, to be able to maintain the connection
 * and check the status of the current connection as new Activities are opened.
 */

public class ConnectionSingleton {

    private static ConnectionSingleton active = null;

    public static ConnectionHandler connectionHandler;

    protected ConnectionSingleton(ConnectionHandler connectionHandler){
        this.connectionHandler = connectionHandler;
    }
    /**
     * Singleton pattern to make sure only one instance of this class will be created throughtout the application
     * lifetime
     * @return an instance of this class
     */
    public static synchronized ConnectionSingleton getInstance(){
        if(null == active){
            active = new ConnectionSingleton(connectionHandler);
        }
        return active;
    }
}
