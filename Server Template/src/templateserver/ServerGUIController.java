/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package templateserver;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
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
import socketfx.FxSocketServer;
import socketfx.SocketListener;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */

//ASK WHY IMAGES AREN'T SHOWING UP


public class ServerGUIController implements Initializable {

    @FXML
    private GridPane gPaneServer, gPaneClient;
    @FXML
    private Button sendButton;
    @FXML
    private TextField sendTextField;
    @FXML
    private Button connectButton, startButton,resetButton;
    @FXML
    private TextField portTextField;
    @FXML
    private Label lblMessages, winnerLabel;
    @FXML
    private ImageView imgS0,imgS1,imgS2,imgS3,imgS4,imgS5,imgS6,imgS7,imgS8,imgS9,
            
                      imgC0,imgC1,imgC2,imgC3,imgC4, imgC5,imgC6,imgC7,imgC8,imgC9, imgDeck,imgDiscard, imgDeck2, imgDiscard2;


    


    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private boolean isConnected, turn = true, serverUNO = false, clientUNO = false;
    private int counter = 0;
    private String color;

    public enum ConnectionDisplayState {
           
        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }

    private FxSocketServer socket;

    private void connect() {
        socket = new FxSocketServer(new FxSocketListener(),
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
    
    
    //I mainly used this method as a way to keep certain buttons, gridpanes, etc. invisible until the actual game begins
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        name1 = JOptionPane.showInputDialog("Enter player 1 name");
        name2 = JOptionPane.showInputDialog("Enter player 2 name");
        startButton.setVisible(false);
        gPaneServer.setVisible(false);
        gPaneClient.setVisible(false);
//        portTextField.setVisible(false);
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
        resetButton.setVisible(false);
        resetButton.setDisable(true);
        imgDeck.setVisible(false);
        imgDiscard.setVisible(false);
        imgDeck2.setVisible(false);
        imgDiscard2.setVisible(false);
        topList.setVisible(false);
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

    class FxSocketListener implements SocketListener { //This method recieves the string messages sent by the client

        @Override
        public void onMessage(String line) {
            lblMessages.setText(line);
            System.out.println("Message received server");
            checkBothStart(line); //Once a message is recieved, it is sent to the checkBothStart method to be substringed and checked
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
           
        }
    }
    public void checkBothStart(String n1){ //This method recieves the messages sent as strings and substrings them.
        //Depending on what message is sent, it will change what certain methods are called
        if(n1.equals("start")){ //If the message is start, displayBoard method will be called.
                displayBoard();  //DisplayBoard() sets up the cards that each person will have.
            
            
        }
//        else if(n1.substring(0,4).equals("disc")){ 
//            String photo = n1.substring(n1.lastIndexOf(":")+1);
//            imgDiscard.setImage((new Image(photo)));
//        }
        else if(n1.substring(0,4).equals("Need")){ //This message is sent by the client when they are drawing a card
            if(clientDeck.size() > 0){ //If the client deck has cards left to draw from
                int temp = (int)(Math.random()*clientDeck.size()); //randomly pick a card from the client pile
                socket.sendMessage("draw:"+clientDeck.get(temp).getCardPath()); //Send the card path to be displayed on the client side
                hand2D.add(clientDeck.remove(temp));  //remove the card from the client deck
                beginner(); //This beginner method is called often to make sure that it is possible for each user to place a card
                if(clientDeck.isEmpty()){ //If the client deck is empty
                    socket.sendMessage("nocards");
                  
                }
            }
            
        }
        else if(n1.substring(0,4).equals("rid:")){ //This method is called when the client is trying to discard a card
            //THIS IS TO CHECK STUFF FOR THE CLIENT HAND, NOT THE SERVER HAND
            int imgClicked = Integer.parseInt(n1.substring(n1.lastIndexOf(":")+1)); //This gets the position in the gridPane that the client clicked on
            int gamer = checkMatches2(imgClicked); //checkMatches2 is a method that checks to see if any of the cards that the user has contains a match with the playing deck. 
            //If there is a possible move for a user, a non-zero integer will be returned/
            if(gamer != 0){ //Gamer not equal to zero means there is a possible move, while gamer = 0 means there is not
                socket.sendMessage("true"); //Telling the client a move is possible
                
                //**Remember, there are two possible discard piles to choose from
                if(gamer == 3 || gamer == 1){ //1 or 3 refer to a match in the first discard pile
                    cardNum1 = hand2D.get(imgClicked).getCardNumber(); //If a card is discarded from your pile, the discard deck takes your card and remembers its number and suite
                    cardSuite1 = hand2D.get(imgClicked).getCardSuit();
                }
                else{ //2 or 4 refer to a possible match in the second discard pile
                    cardNum2 = hand2D.get(imgClicked).getCardNumber();
                    cardSuite2 = hand2D.get(imgClicked).getCardSuit();
                }
                discard = hand2D.remove(imgClicked); //Remove the card from the client's hand
                //Once the card is discarded, the image on the new discard pile is changed
                if(gamer == 1 || gamer == 3){
                    imgDiscard.setImage(new Image(discard.getCardPath()));
                    socket.sendMessage("newdeck:" +discard.getCardPath());
                }
                else if(gamer == 4 || gamer == 2){
                    imgDiscard2.setImage(new Image(discard.getCardPath()));
                    socket.sendMessage("your:" +discard.getCardPath());
                }
                for(int i = 0; i < hand2D.size(); i++){ //This shifts all the card's in the client's hand to the left
                    socket.sendMessage("slide:"+hand2D.get(i).getCardPath() +"|"+i);
                }
                beginner();
                if(hand2D.isEmpty()){
                    winLose();
                }
            }

        }
        else if(n1.substring(0,4).equals("empt")){
            imgDeck2.setImage(null); //If the client's deck is empty, the image is null
        }
        else if(n1.substring(0,4).equals("hand")){ //This method is used to change the image of the opponent's hand based on size.
            //For example, if the user get's rid of a card and has 3 cards, the opponent's number of cards will change
            int photo = Integer.parseInt(n1.substring(n1.lastIndexOf(":")+1));
            for(ImageView x: hand2I)
                x.setImage(null);
            for(int i = 0; i < photo; i++){
                hand2I.get(i).setImage(new Image("resources/BACK-2.jpg"));
            }
        }
        else if(n1.substring(0,4).equals("more")){ //This increases the opponent's hand based on size
            //If the opponent drew a card and now has 4 instead of 3 cards, then there hand will change
            int photo = Integer.parseInt(n1.substring(n1.lastIndexOf(":")+1));
            for(ImageView x: hand2I)
                x.setImage(null);
            for(int i = 0; i < photo; i++){
                hand2I.get(i).setImage(new Image("resources/BACK-2.jpg"));
            }
        }
    }
    public int checkMatches2(int n1){ //This method is used to check if the card the client is trying to discard is the same suite or number as the first card in the discard pile.
        int imgClicked = n1;
        if(hand2D.get(imgClicked).getCardNumber() == cardNum1){ //Discard pile 1; same number 
            clientMoves++;          
            serverMoves = 0;
            clientSuite = 0;
            return 1; 
        }
        else if(hand2D.get(imgClicked).getCardNumber() == cardNum2 ){//Discard pile 2; same number         
            clientMoves++;  
            clientSuite = 0;
            return 2;
        }
        else if(hand2D.get(imgClicked).getCardSuit().equals(cardSuite1)){//Discard pile 1; same suite                       
            clientMoves++;
            clientSuite++;
            clientSuite = 0;
            return 3;
        }
        else if(hand2D.get(imgClicked).getCardSuit().equals(cardSuite2)){//Discard pile 2; same suite         
            clientMoves++;
            clientSuite++;
            clientSuite = 0;
            return 4;
        }
        return 0; //If ther is no match
    }
   
    public int checkers2(int n1){ //This method is used to check if the card the client is trying to discard is the same suite or number as the first card in the discard pile.
        int imgClicked = n1;
        if(hand2D.get(imgClicked).getCardNumber() == cardNum1){ //Discard pile 1; same number ;
            return 1; 
        }
        else if(hand2D.get(imgClicked).getCardNumber() == cardNum2 ){//Discard pile 2; same number         
            return 2;
        }
        else if(hand2D.get(imgClicked).getCardSuit().equals(cardSuite1)){//Discard pile 1; same suite                       
            return 3;
        }
        else if(hand2D.get(imgClicked).getCardSuit().equals(cardSuite2)){//Discard pile 2; same suite         
            return 4;
        }
        return 0; //If ther is no match
    }
    @FXML
    private void handleSendMessageButton(ActionEvent event) { 
        if (!sendTextField.getText().equals("")) {
            socket.sendMessage(sendTextField.getText());
        }
    }
    @FXML
    private void startGame(){ //Once the server and client are connected, the SERVER must press start first
        //The server pressing first ensures that the cards are dealt properly
        startButton.setVisible(false);
        startButton.setDisable(true);
        socket.sendMessage("start");
    }
    @FXML
    private void handleConnectButton(ActionEvent event) { //This method checks to make sure that the cards are displayed properly
        connectButton.setDisable(true);
        connectButton.setVisible(false);
        startButton.setVisible(true);
        displayGame();
        displayState(ConnectionDisplayState.WAITING);
        connect();
    }
    private long startTime;  
    private long cardTime;
    boolean loadCheck = false;
    @FXML
    private void displayBoard(){ //This method is used to display and deal the cards to the server and client
        if(!loadCheck){
            loadList2();
            loadList();
            loadOld();
            loadCheck =true;
        }
        //Setting the gridPane and imageviews to be visible since the game has started
        gPaneServer.setVisible(true);
        gPaneClient.setVisible(true);
        imgDeck.setDisable(false);
        imgDiscard.setDisable(false);
        imgDeck2.setDisable(false);
        imgDiscard2.setDisable(false);
        imgDeck.setVisible(true);
        imgDiscard.setVisible(true);
        imgDeck2.setVisible(true);
        imgDiscard2.setVisible(true);
        for(int i = 0; i < hand1I.size(); i++){
            hand1I.get(i).setImage(null);
        }
        for(int i = 0; i < hand2I.size(); i++){
            hand2I.get(i).setImage(null);
        }
        for(int i = 0;i<4;i++) //Displaying a hand of 4 randomly to the server
        {
            int y = (int)(Math.random()*(52-i));
            hand1I.get(i).setImage(new Image(deck.get(y).getCardPath()));
            hand1D.add(deck.remove(y));
        }
        for(int i = 0;i<4;i++) //Displaying a hand of 4 randomly to the client
        {
            int y = (int)(Math.random()*(48-i));
            socket.sendMessage(deck.get(y).getCardPath());
            hand2D.add(deck.remove(y));
        }
        for(int i = 0; i < hand2D.size(); i++){
            hand2I.get(i).setImage(new Image("resources/BACK-2.jpg"));
        }
//        for (ImageView x: hand2I) //On the server's perspective, they can only see the backside of cards on the client's side
//            x.setImage(new Image("resources/BACK-2.jpg"));
        for(int i = 0; i < hand1D.size(); i++){
            hand1I.get(i).setDisable(false);
        }
        int temp = (int)(Math.random()*deck.size()); 
        //Here, I randomly choose cards to display on the discard piles and remove them from the deck
        imgDeck.setImage(new Image("resources/BACK-1.jpg"));
        imgDiscard.setImage((new Image(deck.get(temp).getCardPath()))); 
        cardNum1 = deck.get(temp).getCardNumber();
        cardSuite1 =  deck.get(temp).getCardSuit();
        socket.sendMessage("discard:"+deck.get(temp).getCardPath()); 
        deck.remove(temp);
        int temp2 = (int)(Math.random()*deck.size());
        imgDeck2.setImage(new Image("resources/BACK-2.jpg"));
        imgDiscard2.setImage((new Image(deck.get(temp2).getCardPath())));
        cardNum2 = deck.get(temp2).getCardNumber();
        cardSuite2 =  deck.get(temp2).getCardSuit();
        socket.sendMessage("your:" + deck.get(temp2).getCardPath());
        deck.remove(temp2);
        setTwoDecks();
        beginner();
        startTime = System.nanoTime();
        cardTime = System.nanoTime();
//        JOptionPane.showMessageDialog(null, "Welcome to my version of Blink! Here are the official rules!"+"\n" + "Each player starts with four cards in their hand." +"\n" + "Every 3 seconds, a new card will be put in your hand." + "\n" + "It is your job to get rid of your cards before your opponent." + "\n" + "However, if you reach 7 cards in your hand, you lose!" + "\n" + "Match according to card suit and number! Good Luck!");
        timeReset = false;
        start();
    }
    public void setTwoDecks(){ //This method splits the deck in half and randomly deals it to the server and client
        int checker = deck.size();
        for(int i = 0; i < checker/2; i++){
            int temp = (int)(Math.random()*deck.size());
            serverDeck.add(deck.remove(temp));
        }
        for(int i = 0; i < checker/2; i++){
            int temp = (int)(Math.random()*deck.size());
            clientDeck.add(deck.remove(temp));
        }
    }
    private String name1 = "";
    private String name2 = "";
    private int scoreCheck = 0;
    private int scoreCheck2 = 0;

    public void insertSort(String n1, int n2){ //This method sorts the list and displays the top ten
        listTop.add(new gamePlay2(n1,n2));
        listTop.get(listTop.size()-1).increaseScore(n2);
        for(int j = 1;j<listTop.size();j++){
            int temp = listTop.get(j).scoreShow();
            String temp2 = listTop.get(j).nameShow();
            int z=j;
            //I made it less than now
            while(z>0 && listTop.get(z-1).scoreShow()<temp){
                listTop.get(z).changer(listTop.get(z-1).nameShow(), listTop.get(z-1).scoreShow());
                z--;
            }
            listTop.get(z).changer(temp2,temp);
        }
        displayList.clear();
        topScores.clear();
        classSaver.clear();
        for(int i = 0; i < listTop.size();  i++){
            if(i < 5){
                String getter = ("Name: " + listTop.get(i).nameShow() + "| Score: " + listTop.get(i).scoreShow());
                String classy = listTop.get(i).nameShow() + "," + listTop.get(i).scoreShow();
                topScores.add(getter);
                classSaver.add(classy);
                displayList.add(getter);
            }
            
        }
        addList2();
        addList();
        
    }
    @FXML
    private void displayGame(){ //This method is called to set the original deck and imageviews
        deck.clear();
        for(int i = 1;i<14;i++)
        {
            deck.add(new Card("C" +Integer.toString(i+1)));
            deck.add(new Card("S"+Integer.toString(i+1)));
            deck.add(new Card("H"+Integer.toString(i+1)));
            deck.add(new Card("D"+Integer.toString(i+1)));   
        }
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
    }
    boolean choose = false;
    @FXML
    private void handleGetCard(MouseEvent event)
    { //This method is called when the server wantst to get a card
        if (hand1D.size()==6) //If the user already has 4 cards, they cannot draw
        {
            System.out.println("You must discard first!");
        }
        else
        {  
            if (serverDeck.size()>0)
            {
                
                    int temp = (int)(Math.random()*serverDeck.size());
                    hand1I.get(hand1D.size()).setImage(new Image(serverDeck.get(temp).getCardPath()));
                    hand1D.add(serverDeck.remove(temp)); 
                    socket.sendMessage("more:"+hand1D.size());
                    for(int i = 0; i < hand1D.size(); i++){
                        hand1I.get(i).setDisable(false); //For this, I disable the imageviews that are unoccupied by a card to make sure the user cannot press there
                    }
                    beginner(); //This might give you an error I'm not sure yet.
                    Timer timer = new Timer();
                    timer.schedule( new TimerTask(){
                        public void run() {
                            choose = true;
                        }
                    }, 2000);
                    choose = true;
                    
                
            }
            if (serverDeck.isEmpty()){ //If the server runs out of cards
                imgDeck.setImage(null);
                socket.sendMessage("empty"); 
                
            }
               
        }
       
            
    }
    @FXML
    private void adderCard(){
        if(hand1D.size() < 8){
            if(serverDeck.size() >0){
                int temp = (int)(Math.random()*serverDeck.size());
                hand1I.get(hand1D.size()).setImage(new Image(serverDeck.get(temp).getCardPath()));
                hand1D.add(serverDeck.remove(temp)); 
                socket.sendMessage("more:"+hand1D.size());
                for(int i = 0; i < hand1D.size(); i++){
                    hand1I.get(i).setDisable(false); //For this, I disable the imageviews that are unoccupied by a card to make sure the user cannot press there
                }
                
            }
            if(serverDeck.isEmpty()){ //If the server runs out of cards
                imgDeck.setImage(null);
                socket.sendMessage("empty"); 
                
            }
            
        }
        if(hand2D.size() < 8){
            if(clientDeck.size()>0){
                int temp = (int)(Math.random()*clientDeck.size()); //randomly pick a card from the client pile
                socket.sendMessage("draw:"+clientDeck.get(temp).getCardPath()); //Send the card path to be displayed on the client side
                hand2D.add(clientDeck.remove(temp));  //remove the card from the client deck
                beginner(); //This beginner method is called often to make sure that it is possible for each user to place a card
                if(clientDeck.isEmpty()){ //If the client deck is empty
                    socket.sendMessage("nocards");
                }
            }
            
            
        }
        
    }
    @FXML
    private void handleDiscard(MouseEvent event) //This is used to discard a card from the server deck
    {
   
            int imgClicked;
            imgClicked =GridPane.getColumnIndex((ImageView) event.getSource()); 
            int gamer = checkMatches(imgClicked); 
            if(gamer != 0){            // Now you are beginning to check matches//////////////////////////////////
                if(gamer == 3 || gamer == 1){
                    cardNum1 = hand1D.get(imgClicked).getCardNumber();
                    cardSuite1 = hand1D.get(imgClicked).getCardSuit();
                }
                else{
                    cardNum2 = hand1D.get(imgClicked).getCardNumber();
                    cardSuite2 = hand1D.get(imgClicked).getCardSuit();
                }
                discard = hand1D.remove(imgClicked);
                if(gamer == 1 || gamer == 3){
                    imgDiscard.setImage(new Image(discard.getCardPath()));
                    socket.sendMessage("discard:"+discard.getCardPath());
           
                }
                else if(gamer == 4 || gamer == 2){
                    imgDiscard2.setImage(new Image(discard.getCardPath()));
                    socket.sendMessage("your:"+discard.getCardPath());
                }
                for(ImageView x: hand1I){
                    x.setImage(null);
                    x.setDisable(true);
                }
                
                for(int i = 0;i<hand1D.size();i++)
                {
                    hand1I.get(i).setImage(new Image(hand1D.get(i).getCardPath()));
                    hand1I.get(i).setDisable(false);
                }
            
                socket.sendMessage("hand:" + hand1D.size());
                beginner();
                if(hand1D.isEmpty()){
                    winLose();
                    sizeCheck();
                }
            }
        
        
           
        

    }
   
    public int checkMatches(int n1){
        int imgClicked = n1;
        if(hand1D.get(imgClicked).getCardNumber() == cardNum1){
            serverMoves++;
            clientMoves = 0;
            clientSuite = 0;
            return 1;
        }
        else if(hand1D.get(imgClicked).getCardNumber() == cardNum2){
            serverMoves++;
            clientMoves = 0;
            return 2;
        }
        else if(hand1D.get(imgClicked).getCardSuit().equals(cardSuite1)){              
            serverMoves++;
            serverSuite++;
            clientMoves = 0;
            clientSuite = 0;
            return 3;
        }
        else if(hand1D.get(imgClicked).getCardSuit().equals(cardSuite2)){
            serverMoves++;
            serverSuite++;
            clientMoves = 0;
            clientSuite = 0;
            return 4;
        }
        return 0; //Change this
    }
    public int checkers(int n1){
        int imgClicked = n1;
        if(hand1D.get(imgClicked).getCardNumber() == cardNum1){
            return 1;
        }
        else if(hand1D.get(imgClicked).getCardNumber() == cardNum2){
            return 2;
        }
        else if(hand1D.get(imgClicked).getCardSuit().equals(cardSuite1)){              
            return 3;
        }
        else if(hand1D.get(imgClicked).getCardSuit().equals(cardSuite2)){
            return 4;
        }
        return 0; //Change this
    }
    @FXML
    private void handleWrong(MouseEvent event){
        System.out.println("Not your deck!");
    }
    
    public void beginner(){ //This beginner method is constantly ran to make sure that it is possible for any either side to make a move.
        //If it is not possible for either side to make a move, a new cards are displayed
        //This method will keep being called until cards that allow possible moves are shown.
        boolean poss = false;
        boolean poss2 = false;
        if(hand1D.size() == 4 || serverDeck.isEmpty()){
            for(int i =0; i < hand1D.size(); i++){
                if(checkers(i) != 0){
                    poss = true;
                }
            }
        }
        if(hand2D.size() == 4 || clientDeck.isEmpty()){
            for(int i =0; i < hand2D.size(); i++){
                if(checkers2(i) != 0){
                    poss2 = true;
                }
            }
        }
        if((hand1D.size() == 4 || serverDeck.isEmpty()) && (hand2D.size() == 4 || clientDeck.isEmpty())){
            if(!poss && !poss2){
                System.out.println("No cards for either side! Random cards are placed!");
                abstracted();
            }
        }
    }

    public void abstracted(){
        if(!hand1D.isEmpty() && !hand2D.isEmpty()){
            if(!serverDeck.isEmpty()){
                int temp = (int)(Math.random()*serverDeck.size());
                imgDiscard.setImage(new Image(serverDeck.get(temp).getCardPath()));
                socket.sendMessage("newdeck:"+serverDeck.get(temp).getCardPath());
                cardNum1 = serverDeck.get(temp).getCardNumber();
                cardSuite1 = serverDeck.get(temp).getCardSuit();
                serverDeck.remove(temp);
            }
            if(!clientDeck.isEmpty()){
                int temp2 = (int)(Math.random()*clientDeck.size());
                imgDiscard2.setImage(new Image(clientDeck.get(temp2).getCardPath()));
                socket.sendMessage("your:"+clientDeck.get(temp2).getCardPath());
                cardNum2 = clientDeck.get(temp2).getCardNumber();
                cardSuite2 = clientDeck.get(temp2).getCardSuit();
                clientDeck.remove(temp2);
            }
            beginner();
        }
       
    }
    @FXML
    private void winLose(){
        if(hand1D.isEmpty() && serverDeck.isEmpty()){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("Server wins!");
            thingy();
            socket.sendMessage("server");
        }
        else if(hand2D.isEmpty() && clientDeck.isEmpty()){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("Client wins!");
            thingy();
            socket.sendMessage("client");
        }
       
    }
    public void sizeCheck(){
        if(hand1D.size() >= 8){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("Client wins!");
            thingy();
            scoreCheck++;
            insertSort(name1,scoreCheck);
            
            socket.sendMessage("client");
        }
        if(hand2D.size() >= 8){
            winnerLabel.setVisible(true);
            winnerLabel.setDisable(false);
            winnerLabel.setText("Server wins!");
            thingy();
            scoreCheck2++;
            insertSort(name2,scoreCheck2);
            socket.sendMessage("server");
        }
    }
    public void thingy(){
        
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
        resetButton.setVisible(true);
        resetButton.setDisable(false);
        timeReset = true;
        topList.setVisible(true);
    }
    @FXML
    private void resetFunction(){
        resetButton.setVisible(false);
        resetButton.setDisable(true);
        winnerLabel.setVisible(false);
        winnerLabel.setDisable(true);
        topList.setVisible(false);
        deck.clear();
        hand1I.clear();
        hand2I.clear();
        hand1D.clear();
        hand2D.clear();
        serverDeck.clear();
        clientDeck.clear();
        cardNum1 = 0;
        cardSuite1 = "";
        cardNum2 = 0;
        serverMoves = 0;
        clientMoves = 0;
        clientSuite = 0;
        serverSuite = 0;
        cardSuite2 = "";
        socket.sendMessage("reset");
        displayGame();
        displayBoard();
    }
    public void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(startTime > 0){
                    if(now - cardTime > 3000000000.0){
                        adderCard();
                        consecutive2();
                        consecutive1();
                        sizeCheck();
                        if(timeReset){
                            this.stop();
                        }  
                        cardTime = now;
                    }
                }
                
            }
            
        }.start();
  
    }
    public void consecutive2(){ //If server or client places three cards consecutively without other person doing so
        if(clientMoves >= 3){
            clientMoves = 0;
            if(hand1D.size() < 7){
                if(serverDeck.size() >0){
                    int temp = (int)(Math.random()*serverDeck.size());
                    hand1I.get(hand1D.size()).setImage(new Image(serverDeck.get(temp).getCardPath()));
                    hand1D.add(serverDeck.remove(temp)); 
                    socket.sendMessage("more:"+hand1D.size());
                    for(int i = 0; i < hand1D.size(); i++){
                        hand1I.get(i).setDisable(false); //For this, I disable the imageviews that are unoccupied by a card to make sure the user cannot press there
                    }
                
                }
                if(serverDeck.isEmpty()){ //If the server runs out of cards
                    imgDeck.setImage(null);
                    socket.sendMessage("empty"); 
                
                }
            
            }
        }
        else if(serverMoves >= 3){
            serverMoves = 0;
            if(hand2D.size() < 7){
                if(clientDeck.size()>0){
                    int temp = (int)(Math.random()*clientDeck.size()); //randomly pick a card from the client pile
                    socket.sendMessage("draw:"+clientDeck.get(temp).getCardPath()); //Send the card path to be displayed on the client side
                    hand2D.add(clientDeck.remove(temp));  //remove the card from the client deck
                    beginner(); //This beginner method is called often to make sure that it is possible for each user to place a card
                    if(clientDeck.isEmpty()){ //If the client deck is empty
                        socket.sendMessage("nocards");
                    }
                }
            
            
            }
        }
        
        
    }
    public void consecutive1(){
        if(serverSuite == 3){
            serverSuite = 0;
            for(int x = 0; x < hand1D.size(); x++){
                int gamer = checkMatches(x);
                if(gamer != 0){            
                    if(gamer == 3 || gamer == 1){
                        cardNum1 = hand1D.get(x).getCardNumber();
                        cardSuite1 = hand1D.get(x).getCardSuit();
                    }
                    else{
                        cardNum2 = hand1D.get(x).getCardNumber();
                        cardSuite2 = hand1D.get(x).getCardSuit();
                    }
                    discard = hand1D.remove(x);
                    if(gamer == 1 || gamer == 3){
                        imgDiscard.setImage(new Image(discard.getCardPath()));
                        socket.sendMessage("discard:"+discard.getCardPath());
           
                    }
                    else if(gamer == 4 || gamer == 2){
                        imgDiscard2.setImage(new Image(discard.getCardPath()));
                        socket.sendMessage("your:"+discard.getCardPath());
                    }
                    for(ImageView y: hand1I){
                        y.setImage(null);
                        y.setDisable(true);
                    }
                
                    for(int i = 0;i<hand1D.size();i++)
                    {
                        hand1I.get(i).setImage(new Image(hand1D.get(i).getCardPath()));
                        hand1I.get(i).setDisable(false);
                    }
            
                    socket.sendMessage("hand:" + hand1D.size());
                    beginner();
                    if(hand1D.isEmpty()){
                        winLose();
                        sizeCheck();
                    }
                }
            }
        }
        else if(clientSuite == 3){
            clientSuite = 0;
            for(int y = 0; y <hand2D.size(); y++){
                int gamer = checkMatches2(y); //checkMatches2 is a method that checks to see if any of the cards that the user has contains a match with the playing deck. 
                //If there is a possible move for a user, a non-zero integer will be returned/
                if(gamer != 0){ //Gamer not equal to zero means there is a possible move, while gamer = 0 means there is not
                    socket.sendMessage("true"); //Telling the client a move is possible
                
                    //**Remember, there are two possible discard piles to choose from
                    if(gamer == 3 || gamer == 1){ //1 or 3 refer to a match in the first discard pile
                        cardNum1 = hand2D.get(y).getCardNumber(); //If a card is discarded from your pile, the discard deck takes your card and remembers its number and suite
                        cardSuite1 = hand2D.get(y).getCardSuit();
                    }
                    else{ //2 or 4 refer to a possible match in the second discard pile
                        cardNum2 = hand2D.get(y).getCardNumber();
                        cardSuite2 = hand2D.get(y).getCardSuit();
                    }
                    discard = hand2D.remove(y); //Remove the card from the client's hand
                    //Once the card is discarded, the image on the new discard pile is changed
                    if(gamer == 1 || gamer == 3){
                        imgDiscard.setImage(new Image(discard.getCardPath()));
                        socket.sendMessage("newdeck:" +discard.getCardPath());
                    }
                    else if(gamer == 4 || gamer == 2){
                        imgDiscard2.setImage(new Image(discard.getCardPath()));
                        socket.sendMessage("your:" +discard.getCardPath());
                    }
                    for(int i = 0; i < hand2D.size(); i++){ //This shifts all the card's in the client's hand to the left
                        socket.sendMessage("slide:"+hand2D.get(i).getCardPath() +"|"+i);
                    }
                    beginner();
                    if(hand2D.isEmpty()){
                        winLose();
                    }
            }
            }
        }
            
    }
    @FXML //This method is used to load the list of the top five scores
    private void loadList(){
        try{
            FileReader reader = new FileReader("src/resources/top.txt");
            Scanner in = new Scanner(reader);
            while(in.hasNextLine())
            {
                String temp = in.nextLine();
                topScores.add(temp);
                displayList.add(temp);
                topList.setItems(displayList);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("SOMETHING HAS GONE HORRIBLY WRONG WE'RE ALL GONNA DIE!");
        }
    }
    @FXML //This method is used to save the top five scores every time the game adds a new user
    private void saveList(){
        String outFile = "src/resources/top.txt";
        try {
                PrintWriter out = new PrintWriter(outFile);
                for(int i = 0; i < topScores.size(); i++)
                {
                    out.println(topScores.get(i));
                }
                out.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerGUIController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something went wrong!");
        }
    }
    @FXML
    private void addList(){ // this method is used to change the listview once a new player plays
        topList.setItems(displayList);
        saveList();
    }
    ////////////////////////////////////////////////////////////////
    @FXML
    private void loadList2(){ //This second loadList is used to load the list people who have played before.
        try{
            FileReader reader = new FileReader("src/resources/class.txt");
            Scanner in = new Scanner(reader);
            while(in.hasNextLine())
            {
                String temp = in.nextLine();
                classSaver.add(temp);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("SOMETHING HAS GONE HORRIBLY WRONG WE'RE ALL GONNA DIE!");
        }
    }
    @FXML
    private void saveList2(){ //This saves the top five so that people playing in the next game can play.
        String outFile = "src/resources/class.txt";
        try {
                PrintWriter out = new PrintWriter(outFile);
                for(int i = 0; i < classSaver.size(); i++)
                {
                    out.println(classSaver.get(i));
                }
                out.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerGUIController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something went wrong!");
        }
    }
    @FXML
    private void addList2(){
        saveList2();
    }
    private void loadOld(){ //This method is used to keep track of the five best players after a game ends.
        for(int i = 0; i < classSaver.size(); i++){
            String total = classSaver.get(i);
            int check1 = total.indexOf(",");
            String name1 = total.substring(0,check1);
            System.out.println("Name1: " + name1);
            int score1 = Integer.parseInt(total.substring(total.lastIndexOf(",")+1));
            System.out.println("Score1: " + score1);
            listTop.add(new gamePlay2(name1,score1));
        }
    }
    List<Card> deck = new ArrayList<>();
    List<Card> serverDeck = new ArrayList<>();
    List<Card> clientDeck = new ArrayList<>();
    Card discard;
    List<ImageView> hand1I = new ArrayList<>();
    List<ImageView> hand2I = new ArrayList<>();
    List<Card> hand1D = new ArrayList<>();
    List<Card> hand2D = new ArrayList<>();
    private int cardNum1 = 0;
    private String cardSuite1 = "";
    private int cardNum2 = 0;
    private String cardSuite2 = "";
    private boolean timeReset = false;
    private ObservableList displayList = FXCollections.observableArrayList();
    
    private int serverMoves = 0;
    private int serverSuite = 0;
    
    private int clientMoves = 0;
    private int clientSuite = 0;
    
    ArrayList<String> topScores = new ArrayList<>();
    ArrayList<String> classSaver = new ArrayList<>();
    ArrayList<gamePlay2> listTop = new ArrayList<gamePlay2>();
    @FXML
    private ListView topList;


  
    

    
   
    
    
    
}

