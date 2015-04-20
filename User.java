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

import java.util.Scanner;

public class User implements ExceptionListener {
  private Session session;
  public void processConsumer(int option)throws Exception{

    // Create a ConnectionFactory
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
            "tcp://localhost:61616");

    // Create a Connection
    Connection connection = connectionFactory.createConnection();
    //connection.start();

    connection.setExceptionListener(this);

    // Create a Session
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    //Destination destination = session.createTopic("MyTopic");
    //MessageConsumer consumer = session.createConsumer(destination);
    //consumer.setMessageListener(null);
    //destination = session.createTopic(channelName);
    //consumer = session.createConsumer(destination);
    System.out.println("Entro al metodo");
    switch(option){
      case 1: System.out.println("Listening Cars");
              Destination destination1 = session.createTopic("Cars");
              MessageConsumer consumer1 = session.createConsumer(destination1);
              consumer1.setMessageListener(listener);
              connection.start();
              break;
      case 2: Destination destination2 = session.createTopic("Sports");
              MessageConsumer consumer2 = session.createConsumer(destination2);
              consumer2.setMessageListener(listener);
              connection.start();
              break;
      case 3: Destination destination3 = session.createTopic("Money");
              MessageConsumer consumer3 = session.createConsumer(destination3);
              consumer3.setMessageListener(listener);
              connection.start();
              break;
    }

    //consumer1.close();
    //session.close();
    //connection.close();

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
              System.out.println("------------------------");
          } else {
              System.out.println("Message: " + msg);
              System.out.println("------------------------");
          }
      }
  };

  public synchronized void onException(JMSException ex) {
      System.out.println("JMS Exception occured.  Shutting down client.");
  }

  public static void main(String[] args) throws Exception {
    User p = new User();
    System.out.println("Select the topic you would like to recieve info");
    System.out.println("1. Cars");
    System.out.println("2. Sports");
    System.out.println("3. Money");
    Scanner scanner = new Scanner(System.in);
    switch(scanner.nextInt()){
      case 1: p.processConsumer(1);
              System.out.println("ENTRO SWITCH");
              break;
      case 2: p.processConsumer(2);
              break;
      case 3: p.processConsumer(3);
              break;
      default: System.out.println("Input error");
      }
    }
  }
