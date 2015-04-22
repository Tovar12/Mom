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



public class Rss implements ExceptionListener {

  private Session session;
  public void processProducer(int option){
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
      Destination destination = session.createTopic("MyTOPIC");
      MessageProducer producer = session.createProducer(destination);

      switch(option){
        case 1: Destination destination1 = session.createTopic("Cars");
                MessageProducer producer1 = session.createProducer(destination1);
                sendMessage1(producer1);
                break;
        case 2: Destination destination2 = session.createTopic("Sports");
                MessageProducer producer2 = session.createProducer(destination2);
                sendMessage2(producer2);
                break;
        case 3: Destination destination3 = session.createTopic("Money");
                MessageProducer producer3 = session.createProducer(destination3);
                sendMessage3(producer3);
                break;
        default: System.out.println("Input error");

      }
      // Clean up
			session.close();
			connection.close();


    } catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}


  }

  public void sendMessage1(MessageProducer producer1) throws Exception{
    for(int i = 0; i < 20; i++){
      // Create a messages
      String text = "Hola Mundo!" + i;
      TextMessage message = session.createTextMessage(text);
      // Tell the producer to send the message
      System.out.println("Mensaje Enviado Cars: " + text);
      //producer.send(message);
      producer1.send(message);
    }
  }
  public void sendMessage2(MessageProducer producer2)throws Exception{
    for(int i = 0; i < 20; i++){
      // Create a messages
      String text = "Hola Mundo!" + i;
      TextMessage message = session.createTextMessage(text);
      // Tell the producer to send the message
      System.out.println("Mensaje Enviado Sports: " + text);
      //producer.send(message);
      producer2.send(message);
    }
  }
  public void sendMessage3(MessageProducer producer3)throws Exception{
    for(int i = 0; i < 20; i++){
      // Create a messages
      String text = "Hola Mundo!" + i;
      TextMessage message = session.createTextMessage(text);
      // Tell the producer to send the message
      System.out.println("Mensaje Enviado Money: " + text);
      //producer.send(message);
      producer3.send(message);
    }
  }

  public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
	}

	public static void main(String[] args) throws Exception {
		Rss p = new Rss();
    System.out.println("Select the rss you would like to recieve info");
    System.out.println("1. Cars");
    System.out.println("2. Sports");
    System.out.println("3. Money");
    Scanner scanner = new Scanner(System.in);
    switch(scanner.nextInt()){
      case 1: p.processProducer(1);
              break;
      case 2: p.processProducer(2);
              break;
      case 3: p.processProducer(3);
              break;
      default: System.out.println("Input error");

    }
	}
}



/**

public void Productor {
  private static Productor instace;

  private Productor() {

  }

  public static Productor getInstance() {
    if (instance == null) instance = new Productor();
    return instance;
  }
}

public class Main {
  public static void main (String [] args) {
    Productor miProductor = Productor.getInstance();
    miProductor.miMetodo();
  }
}

**/
