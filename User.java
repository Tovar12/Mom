import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.Iterator;

public class User implements ExceptionListener {
  private Session session;
  private Connection connection;
  private Destination destination;
  private MessageConsumer consumer;
  private Boolean isListening = true;
  private Boolean hasTopic = true;
  private String userTopic;
  private Scanner scanner = new Scanner (System.in);
  private ArrayList<String> topicsAvailable = new ArrayList<String>();

//Here we create the connection and subscribe the user to a default topic
  public void processConsumer() throws Exception{
    // Create a ConnectionFactory
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
            "tcp://localhost:61616");
    // Create a Connection
    connection = connectionFactory.createConnection();
    connection.setExceptionListener(this);
    // Create a Session
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    System.out.println("You have been subscribed to our default topic");
    destination = session.createTopic("default");
    consumer = session.createConsumer(destination);
    userTopic = "default";
    receiveInput();
  }

  MessageListener listener = new MessageListener() {
      @Override
      public void onMessage(Message msg) {
          if (msg instanceof TextMessage) {
              TextMessage textMessage = (TextMessage) msg;
              String text = null;
              try {
                  text = textMessage.getText();
              } catch (JMSException e) {
                  e.printStackTrace();
              }
              System.out.println("Message: " + text);
          } else {
              System.out.println("Message: " + msg);
          }
      }
  };
  /**
  This method has a menu that allow the user between listen a new topic, stop
  listening a topic or start listening again the topic.
  **/
  public void receiveInput() throws Exception{
    while(true){
      System.out.println("Menu: ");
      System.out.println("1. Listen new topic");
      if(hasTopic){
        //Evaluate if the user is available to listen the message of his topic.
        if(isListening){
          System.out.println("2. TurnOff connection");
        }else{
          System.out.println("2. TurnOn connection");
        }
        System.out.println("3. Stop listening a topic");
        System.out.println("4. Exit");
        //Wait the input of the user
        switch(scanner.nextInt()){
          case 1: listenNewTopic();
                  break;
          case 2: turnOnOrOff();
                  break;
          case 3: stopListening();
                  break;
          case 4: System.out.println("Bye");
                  System.exit(1);
                  break;
          default: System.out.println("Input error");
          }
      } else {
        System.out.println("2. Exit");
        //Wait the input of the user
        switch(scanner.nextInt()){
          case 1: listenNewTopic();
                  break;
          case 2: System.out.println("Bye");
                  System.exit(1);
                  break;
          default: System.out.println("Input error");
          }
      }
    }
  }

  public void stopListening() throws Exception{
    System.out.println("You aren't listening a topic");
    //destination = session.createTopic("x");
    //consumer = session.createConsumer(destination);
    consumer.close();
    hasTopic = false;
  }

  //Start listening for message
  public void listen() throws Exception{
    consumer.setMessageListener(listener);
    connection.start();
  }
  //Receive an input an listen to that topic
  public void listenNewTopic() throws Exception{
    readFile();
    selectNewTopic();
  }
/**
  Start or stop listening a topic, if the user decide to stop listening a topic
  the message will be save and when the user decide to start listening again he
  will receive the messages that were sent when he was offline.
  **/
  public void turnOnOrOff() throws Exception {
    if(isListening){
      System.out.println("Connection turned off");
      isListening = false;
      consumer.setMessageListener(null);
    }else{
      System.out.println("Connection turned on");
      isListening = true;
      consumer.setMessageListener(listener);
    }
  }

  public void selectNewTopic(){
    try{
      File file = new File("topics.txt");
      FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
      try{
        //Block the file to avoid other people write or read the file
        FileLock lock = channel.tryLock();
        while (!lock.isValid())
                lock = channel.lock();

        if(lock.isValid()){
          try{
            if(topicsAvailable.size() != 0){
              System.out.println("These are the topics availables");
              showLines();
              scanner.nextLine();
              System.out.println("Select the topic:");
              for(int i = 0; i < topicsAvailable.size(); i++){
                System.out.println((i+1) + ". " + topicsAvailable.get(i));
              }
              int topicSelected = scanner.nextInt();
              destination = session.
                createTopic(topicsAvailable.get(topicSelected-1));
              consumer = session.createConsumer(destination);
              System.out.println("_________________________________________");
              System.out.println("You are now listening *" +
                                  topicsAvailable.get(topicSelected-1) + "*");
              System.out.println("_________________________________________");
              hasTopic = true;
              listen();

            } else {
              System.out.println("There is no topics availables");
            }
          }catch(Exception e){
            System.out.println("The topics can't be displayed at this moment");
            e.printStackTrace();
          }
        }
        lock.release();
      }catch(Exception e){
        System.out.println("Archivo Bloqueado");
      }
      channel.close();
    }catch(Exception e){
      System.out.println("Archivo Bloqueado");
    }
  }

  public boolean readFile(){
    try{
      File file = new File("topics.txt");
      FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
      try{
        //Block the file to avoid other people write or read the file
        FileLock lock = channel.tryLock();
        try{
          if (file.length() != 0) {
            long channelSize = channel.size();
            ByteBuffer buf = ByteBuffer.allocate((int) channelSize);
            channel.read(buf);
            buf.rewind();
            String allInput = new String(buf.array(), "UTF-8");
            String[] parts = allInput.split("\n");            
            topicsAvailable.clear();
            Collections.addAll(topicsAvailable, parts);
            Collections.sort(topicsAvailable);
          }
          }catch(Exception e){
            System.out.println("The topics can't be displayed at this moment");
            e.printStackTrace();
            return false;
          }
          lock.release();
      }catch(Exception e){
        System.out.println("Archivo Bloqueado");
        return false;
      }
      channel.close();
    }catch(Exception e){
      System.out.println("Archivo Bloqueado");
      return false;
    }
    return true;
  }

  public void showLines(){
    System.out.println("-----------------------------------------------------");
  }

  public synchronized void onException(JMSException ex) {
      System.out.println("JMS Exception occured.  Shutting down client.");
  }

  public static void main(String[] args) throws Exception {
    User p = new User();
    p.processConsumer();
    }
  }
