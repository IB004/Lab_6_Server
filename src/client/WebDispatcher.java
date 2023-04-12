package client;

import data.CommandData;
import data.LabWork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class WebDispatcher {
    private SocketChannel socketChannel;
    private ByteBuffer buf = ByteBuffer.allocate(4096);

    ByteArrayOutputStream commandDataByteStream = new ByteArrayOutputStream();
    ObjectOutputStream commandDataObjectStream;



    public WebDispatcher(Message messageComponent){
        this.messageComponent = messageComponent;
    }

    Message messageComponent;
    public SocketChannel connect(String host, int port) throws Exception{
        InetSocketAddress address = new InetSocketAddress(host, port);
        boolean isConnected = false;
        //try to connect
        while(!isConnected) {
            try {
                socketChannel = SocketChannel.open(address);
                isConnected = socketChannel.isConnected();
            } catch (ConnectException e) {
                System.out.println("Try to connect ... ");
                Thread.sleep(3000);
            }
        }
        socketChannel.configureBlocking(false);
        createStreams();
        messageComponent.connectionSuccess();
        return socketChannel;
    }

    public void writeCommandData(CommandData commandData) throws IOException {

    }

    public void createStreams() throws Exception {
        commandDataObjectStream = new ObjectOutputStream(commandDataByteStream);
    }


    public void sendTestBytes() throws Exception{
        InetSocketAddress address = new InetSocketAddress(        "127.0.0.1", 8888);
        SocketChannel socketChannel = null;
        boolean isConnected = false;

        //try to connect
        while(!isConnected) {
            try {
                socketChannel = SocketChannel.open(address);
                isConnected = socketChannel.isConnected();
            } catch (ConnectException e) {
                System.out.println("Try to connect ... ");
                Thread.sleep(1000);
            }
        }
            socketChannel.configureBlocking(false);

        LabWork labWork = getTestObject();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream ous = new ObjectOutputStream(byteStream);
        ous.writeObject(labWork);
        ByteBuffer buf = ByteBuffer.wrap(byteStream.toByteArray());
        socketChannel.write(buf);

        System.out.println("Client send: " + byteStream.toString());
        socketChannel.close();
        while (true) {}
    }

    public void sendCommandDataToExecutor(CommandData commandData) throws IOException {
        if (commandData.command.isClientCommand()){
            sendCommandToClient(commandData);
        }
        sendCommandToServer(commandData);
    }

    private void sendCommandToServer(CommandData commandData) throws IOException {
        commandDataObjectStream.reset();
        commandDataObjectStream.writeObject(commandData);

        //add 4 to write length
        ByteBuffer buffer = ByteBuffer.allocate(4 + commandDataByteStream.size());
        buffer.putInt(0, commandDataByteStream.size());
        buffer.put(4, commandDataByteStream.toByteArray());
        buffer.position(0);
        socketChannel.write(buffer);
        System.out.println("Send " + (commandDataByteStream.size() + 4) + " bytes to server:");
        System.out.println(Arrays.toString(buffer.array()));
    }

    private void sendCommandToClient(CommandData commandData){
        commandData.client.execute(commandData);
    }

    public void getResultDataFromServer(){
    }

    private LabWork getTestObject() throws Exception{
        LabWork labWork = new LabWork();
        labWork.setId(1);
        labWork.setName("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
                "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
                "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        labWork.setMinimalPoint("1");
        labWork.setMaximumPoint("100000000000");
        labWork.setPersonalQualitiesMaximum("3");
        labWork.setCoordinatesX("4");
        labWork.setCoordinatesY("5");
        labWork.setDisciplineName(  "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
                "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
                "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        labWork.setDisciplineLabsCount("6");
        labWork.setDisciplineLabsCount("7");
        labWork.setDifficulty("3");
        return labWork;
    }
}
