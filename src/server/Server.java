package server;

import client.Message;
import client.Warning;

public class Server {
    public Server() throws Exception {
        messageComponent = new Message();
        warningComponent = new Warning();
        webConnector = new WebConnector(messageComponent, warningComponent);
    }
    private final WebConnector webConnector;
    private final Message messageComponent;
    private final Warning warningComponent;

    public void doWhileTrue() {
        try {
            while(true){
                webConnector.serveConnections();
                //check user input
            }
        }
        catch (Exception e){
            warningComponent.warningMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    public void startServerActions(){
        System.out.println("Try to load collection from file");
    }
}
