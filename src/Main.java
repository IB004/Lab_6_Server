
import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.startServerActions();
        server.doWhileTrue();
    }

}


/*
    // in start method should fill the collection from the CSV file
    public void askToReadFile(){
        CommandData commandData = new CommandData();
        commandData.command = new ReadCommand();
        //ResultData resultData = sendCommandToExecutor(commandData);
        //ResultHandler.showResult(resultData);
        Message.printEmptyLine();
    }
 */