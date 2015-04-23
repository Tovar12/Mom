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

public class User implements ExceptionListener {
  private Session session;
  private Connection connection;
  private Destination destination;
  private MessageConsumer consumer;
  private Boolean isListening = true;
  private Boolean hasTopic = true;
  private String userTopic;
  private Scanner scanner = new Scanner (System.in);

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
  public void listen()throws Exception{
    consumer.setMessageListener(listener);
    connection.start();
  }
  //Receive an input an listen to that topic
  public void listenNewTopic() throws Exception{
    System.out.println("Write the name of a topic you would like to subscribe:");
    scanner.nextLine();
    userTopic = scanner.nextLine().toLowerCase();
    destination = session.createTopic(userTopic);
    consumer = session.createConsumer(destination);
    System.out.println("_________________________________________");
    System.out.println("You are now listening *" + userTopic + "*");
    System.out.println("_________________________________________");
    hasTopic = true;
    listen();
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

  public synchronized void onException(JMSException ex) {
      System.out.println("JMS Exception occured.  Shutting down client.");
  }

  public static void main(String[] args) throws Exception {
    User p = new User();
    p.processConsumer();
    }
  }
