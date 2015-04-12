

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

public class EjemploConsumidor implements ExceptionListener {

    void processConsumer() {
        String clientID = "edwin";
        try {

            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    "tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            //connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
            //Destination destination = session.createQueue("MyQUEUE");
            Destination destination = session.createTopic("MyTOPIC");
            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);
			//MessageConsumer consumer = session.createDurableSubscriber((Topic) destination, "edwin");

            consumer.setMessageListener(listener);
            connection.start();

	    // Wait for a message
            /*Message message = consumer.receive(1000);

             while (message != null) {
             Thread.sleep(5000);

             if (message instanceof TextMessage) {
             TextMessage textMessage = (TextMessage) message;
             String text = textMessage.getText();
             System.out.println("Received: " + text);
             } else {
             System.out.println("Received: " + message);
             }
             message = consumer.receiveNoWait();
             }
             consumer.close();
             session.close();
             connection.close();
             */
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    MessageListener listener = new MessageListener() {
        public void onMessage(Message msg) {
            if (msg instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) msg;
                String text = null;
                try {
                    text = textMessage.getText();
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Recibido: " + text);
            } else {
                System.out.println("Recibido: " + msg);
            }
        }

    };

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }

    public static void main(String[] args) throws Exception {
        EjemploConsumidor c = new EjemploConsumidor();
        System.out.println("Running Consumer...");
        c.processConsumer();
    }

}
