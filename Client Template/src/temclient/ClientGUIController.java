/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package temclient;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javax.swing.JOptionPane;
import socketfx.Constants;
import socketfx.FxSocketClient;
import socketfx.SocketListener;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */
public class ClientGUIController implements Initializable {


    @FXML
    private Button sendButton, startButton;
    @FXML
    private TextField sendTextField;
    @FXML
    private Button connectButton;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField hostTextField;
    @FXML
    private Label lblName1, lblName2, lblName3, lblName4, lblMessages, winnerLabel;
    @FXML
    private ImageView imgS0,imgS1,imgS2,imgS3,imgS4,imgS5,imgS6,imgS7,imgS8,imgS9,
            
                      imgC0,imgC1,imgC2,imgC3,imgC4, imgC5,imgC6,imgC7,imgC8,imgC9, imgDeck, imgDiscard, imgDeck2, imgDiscard2;
    @FXML
    private GridPane gPaneServer, gPaneClient;

    
    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());



    private boolean isConnected, turn, serverUNO = false, clientUNO = false;

    public enum ConnectionDisplayState {

        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }

    private FxSocketClient socket;
    private void connect() {
        socket = new FxSocketClient(new FxSocketListener(),
                hostTextField.getText(),
                Integer.valueOf(portTextField.getText()),
                Constants.instance().DEBUG_NONE);
        socket.connect();
    }

    private void displayState(ConnectionDisplayState state) {
//        switch (state) {
//            case DISCONNECTED:
//                connectButton.setDisable(false);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case WAITING:
//            case AUTOWAITING:
//                connectButton.setDisable(true);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case CONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//            case AUTOCONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override  //I mainly used this method as a way to keep certain buttons, gridpanes, etc. invisible until the actual game begins
    public void initialize(URL url, ResourceBundle rb) {
        isConnected = false;
        startButton.setVisible(false);
        gPaneClient.setVisible(false);
        gPaneServer.setVisible(false);
//        portTextField.setVisible(false);
//        hostTextField.setVisible(false);
        imgDeck.setDisable(true);
        imgDiscard.setDisable(true);
        imgDeck2.setDisable(true);
        imgDiscard2.setDisable(true);
        sendTextField.setDisable(true);
        sendTextField.setVisible(false);
        sendButton.setVisible(false);
        sendButton.setDisable(true);
        winnerLabel.setDisable(true);
        winnerLabel.setVisible(false);
        imgDeck.setVisible(false);
        imgDiscard.setVisible(false);
        imgDeck2.setVisible(false);
        imgDiscard2.setVisible(false);
        displayState(ConnectionDisplayState.DISCONNECTED);
        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        /*
         * Uncomment to have autoConnect enabled at startup
         */
//        autoConnectCheckBox.setSelected(true);
//        displayState(ConnectionDisplayState.WAITING);
//        connect();
    }

    class ShutDownThread extends Thread {

        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");    
                }
                socket.shutdown();
            }
        }
    }
    
    class FxSocketListener implements SocketListener { 
        @Override
        public void onMessage(String line) {//This method recieves the string messages sent by the client
            System.out.println("message received client");
            lblMessages.setText(line);
            checkBothStart(line);//Once a message is recieved, it is sent to the checkBothStart method to be substringed and checked
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
           
        }
    }
    private int turnBase = 0;
    //Make these into switch statements
    public void checkBothStart(String n1){
        if(n1.equals("start")){ //This message makes sure that the server pressed start first
            started1 = true;
        }
        else if(n1.substring(0,4).equals("reso")){ //This message is called numerous times in a row. It sends which cards are going to be displayed in the client hand.
            displayBoard(n1);
            turnBase++; //This is used to determine the position of the image view that will recieve a card
        }
        else if(n1.substring(0,4).equals("disc")){ //This is used to dsicard a card 
            String photo = n1.substring(n1.lastIndexOf(":")+1);
            imgDiscard.setImage((new Image(photo)));
        }
        else if(n1.substring(0,4).equals("empt")){//If the server runs out of cards in their deck
            imgDeck2.setImage(null);
        }
        else if(n1.substring(0,4).equals("draw")){ //If the client chooses to pick a card
            String photo = n1.substring(n1.lastIndexOf(":")+1);
          
            hand1I.get(handSize).setImage(new Image(photo)); 
            handSize++;
            for(int i = 0; i < handSize; i++){
                hand1I.get(i).setDisable(false);
            }
            socket.sendMessage("more:"+handSize);
        }
        else if(n1.substring(0,4).equals("newd")){ //If the server discarded
            String photo = n1.substring(n1.lastIndexOf(":")+1);
            imgDiscard.setImage(new Image(photo));
        }
        else if(n1.substring(0,4).equals("slid")){ //This will slide all the clients cards to the left as they discard
            
            int num = Integer.parseInt(n1.substring(n1.lastIndexOf("|")+1));
            System.out.println("num: "+num);
            String photo = n1.substring(n1.lastIndexOf(":")+1,n1.indexOf("|"));
            System.out.println("photo: " + photo);
            
            hand1I.get(num).setImage(new Image(photo));

            
        }
        else if(n1.substring(0,4).equals("noca")){
            imgDeck.setImage(null); //I am emptying MY deck
            socket.sendMessage("empty");
        }
        else if(n1.substring(0,4).equals("hand")){ //This will slide the opponent's cards to the left as they discard
            int photo = Integer.parseInt(n1.substring(n1.lastIndexOf(":")+1));
            System.out.println("Photo: " + photo);
            for(ImageView x: hand2I)
                x.setImage(null);
            for(int i = 0; i < photo; i++){
                hand2I.get(i).setImage(new Image("resources/BACK-1.jpg"));
            }
        }
        else if(n1.substring(0,4).equals("more")){ //This will add a card to the opponent's hand as they continue
            int photo = Integer.parseInt(n1.substring(n1.lastIndexOf(":")+1));
            for(ImageView x: hand2I)
                x.setImage(null);
            for(int i = 0; i < photo; i++){
                hand2I.get(i).setImage(new Image("resources/BACK-1.jpg"));
            }
        }
        else if(n1.substring(0,4).equals("your")){ 
            String photo = n1.substring(n1.lastIndexOf(":")+1);
            imgDiscard2.setImage((new Image(photo)));
        }
        else if(n1.substring(0,4).equals("true")){
            displayStuff();
        }
        else if(n1.substring(0,4).equals("serv")){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("server Wins!");
            fixUp();
        }
        else if(n1.substring(0,4).equals("clie")){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("client Wins!");
            fixUp();
        }
        else if(n1.substring(0,4).equals("rese")){
            resetFunction();
            for(int i = 0; i < hand1I.size(); i++){
                hand1I.get(i).setImage(null);
            }
        }


    }
    private boolean sta1 = false;
    private boolean aval = true;
    @FXML
    private void resetFunction(){
        turnBase = 0;
        winnerLabel.setVisible(false);
        winnerLabel.setDisable(true);
        deck.clear();
        hand1D.clear();
        hand1I.clear();
        hand2I.clear();
        displayGame();
    }
    @FXML
    private void fixUp(){
        gPaneServer.setVisible(false);
        gPaneClient.setVisible(false);
        imgDeck.setDisable(true);
        imgDiscard.setDisable(true);
        imgDeck2.setDisable(true);
        imgDiscard2.setDisable(true);
        imgDeck.setVisible(false);
        imgDiscard.setVisible(false);
        imgDeck2.setVisible(false);
        imgDiscard2.setVisible(false);
    }
    @FXML
    public void displayBoard(String n1){ //Used to set up the board and make things visisble
        gPaneClient.setVisible(true);
        gPaneServer.setVisible(true);
        imgDeck.setDisable(false);
        imgDiscard.setDisable(false);
        imgDeck2.setDisable(false);
        imgDiscard2.setDisable(false);
        imgDeck.setVisible(true);
        imgDiscard.setVisible(true);
        imgDeck2.setVisible(true);
        imgDiscard2.setVisible(true);
        hand1I.get(turnBase).setImage(new Image(n1));
        for(int i = 0; i < 4; i++){
            hand2I.get(i).setImage(new Image("resources/BACK-1.jpg"));
        }
//        for (ImageView x: hand2I){
//            x.setImage(new Image("resources/BACK-1.jpg"));
//        }
        for(int i = 0; i < 4; i++){
            hand1I.get(i).setDisable(false);
        }
        imgDeck.setImage(new Image("resources/BACK-2.jpg"));
        imgDeck2.setImage(new Image("resources/BACK-1.jpg"));
        

    }
    
    private int handSize = 0; //Gotta fix this
    @FXML
    public void displayGame(){
        hand1I.add(imgS0);
        hand1I.add(imgS1);
        hand1I.add(imgS2);
        hand1I.add(imgS3);
        hand1I.add(imgS4);
        hand1I.add(imgS5);
        hand1I.add(imgS6);
        //////////////////////////
        hand2I.add(imgC0);
        hand2I.add(imgC1);
        hand2I.add(imgC2);
        hand2I.add(imgC3);
        hand2I.add(imgC4);
        hand2I.add(imgC5);
        hand2I.add(imgC6);
        handSize = 4;
    }

    @FXML
    private void handleSendMessageButton(ActionEvent event) {
        System.out.println("WORKING FOR THE CLIENT");
        if (!sendTextField.getText().equals("")) {
            String x = sendTextField.getText();
            socket.sendMessage(x);
            System.out.println("sent message client");
            
        }

    }
    @FXML
    private void startGame(){
            if(started1){
                startButton.setVisible(false);
                socket.sendMessage("start");  
                startButton.setDisable(true);
            }
            else{
                startButton.setText("Server not started!");
            }

        
    }
    @FXML
    private void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);
        connectButton.setVisible(false);
        startButton.setVisible(true);
        displayGame();
        displayState(ConnectionDisplayState.WAITING);
        connect();
        
    }
    @FXML
    private void handleGetCard(MouseEvent event)
    {
        if (handSize==4)
        {
            System.out.println("You must discard first!");
        }
        else
        {
            
            socket.sendMessage("Need Card");
        }
            
    }
    @FXML
    private void handleDiscard(MouseEvent event)
    {
        int imgClicked;
        imgClicked =GridPane.getColumnIndex((ImageView) event.getSource());
        socket.sendMessage("rid:" + imgClicked); //When I am discarding a card
        
    }
    @FXML 
    private void displayStuff(){
        handSize--;
        for(ImageView x: hand1I){
            x.setImage(null);
            x.setDisable(true);
        }
        for(int i = 0; i < handSize; i++){
            hand1I.get(i).setDisable(false);
        }
        socket.sendMessage("hand:" + handSize);
    }
    
    
    private boolean started1 = false;
    List<Card> deck = new ArrayList<>();
    Card discard;
    List<ImageView> hand1I = new ArrayList<>();
    List<ImageView> hand2I = new ArrayList<>();
    List<Card> hand1D = new ArrayList<>();
//    List<Card> hand2D = new ArrayList<>();
}
