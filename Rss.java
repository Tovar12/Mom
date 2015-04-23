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
import java.util.ArrayList;

public class Rss implements ExceptionListener {

  private ArrayList<String> topicsAvailable = new ArrayList<String>();
  private Scanner scanner = new Scanner(System.in);
  private Session session;
  private Destination destination;
  private MessageProducer producer;
  private static Rss instance;

  private Rss() {
  }

  public void processProducer(){
    try{
      // Create a ConnectionFactory
      ActiveMQConnectionFactory connectionFactory = new
      ActiveMQConnectionFactory("tcp://localhost:61616");

      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.start();

      // Create a Session
      session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
      //Default
      destination = session.createTopic("default");
      producer = session.createProducer(destination);
      startProducing();
      // Clean up
      //session.close();
      //connection.close();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  public void startProducing() throws Exception{
    while(true){
      System.out.println("Menu:");
      System.out.println("1. Create a new topic");
      System.out.println("2. Topics available");
      System.out.println("3. Send a Message");
      System.out.println("4. Delete a topic");
      System.out.println("5. Exit");
      switch(scanner.nextInt()){
        case 1: createNewTopic();
                break;
        case 2: getTopicsAvailable();
                break;
        case 3: sendMessage();
                break;
        case 4: deleteTopic();
                break;
        case 5: System.exit(1);
                break;
        default: System.out.println("Input error");
      }
    }
  }

  public void createNewTopic(){
    System.out.println("Write the name of new topic");
    scanner.nextLine();
    String newTopic = scanner.nextLine().toLowerCase();
    topicsAvailable.add(newTopic);
    System.out.println("The new topic with name " + newTopic +
                                                        " has been created");
  }

  public void getTopicsAvailable(){
    if(topicsAvailable.size() != 0){
      for(int i = 0; i < topicsAvailable.size(); i++){
        System.out.println("| " + topicsAvailable.get(i) + " |");
      }
    }else{
      System.out.println("You don't have topics created");
    }
  }

  public void sendMessage() throws Exception{
    if(topicsAvailable.size() != 0){
      System.out.println("Select the topic:");
      for(int i = 0; i < topicsAvailable.size(); i++){
        System.out.println((i+1) + ". " + topicsAvailable.get(i));
      }
      String topicSelected = topicsAvailable.get(scanner.nextInt()-1);
      destination = session.createTopic(topicSelected);
      producer = session.createProducer(destination);
      System.out.println("Write the message:");
      scanner.nextLine();
      String inputMessage = scanner.nextLine().toLowerCase();
      TextMessage message = session.createTextMessage(inputMessage);
      System.out.println("How many times would you like to send the message:");
      int times = scanner.nextInt();
      for(; times > 0; times--)  producer.send(message);
      System.out.println("The message " + inputMessage + " has been sent");
    }else{
      System.out.println("You don't have topics created");
    }
  }

  public void deleteTopic(){
    if(topicsAvailable.size() != 0){
      System.out.println("These are the topics availables");
      System.out.println("----------------------------------");
      getTopicsAvailable();
      scanner.nextLine();
      System.out.println("----------------------------------");
      System.out.println("write the name of the topic you want to delete:");
      String option = scanner.nextLine().toLowerCase();
      topicsAvailable.remove(option);
    } else {
      System.out.println("You don't have topics created");
    }
  }

  public synchronized void onException(JMSException ex) {
    System.out.println("JMS Exception occured.  Shutting down client.");
  }

  public static Rss getInstance() {
    if (instance == null){
      instance = new Rss();
    }
    return instance;
  }

  public static void main(String[] args) throws Exception {
    Rss rss = Rss.getInstance();
    rss.processProducer();
  }
}
