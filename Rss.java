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
import java.util.List;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.io.PrintWriter;

public class Rss implements ExceptionListener {

  private ArrayList<String> topicsAvailable = new ArrayList<String>();
  public static final String NEWLINE = System.getProperty("line.separator");
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
      System.out.println("4. Modify a topic");
      System.out.println("5. Delete a topic");
      System.out.println("6. Exit");
      switch(scanner.nextInt()){
        case 1: createNewTopic();
                break;
        case 2: getTopicsAvailable();
                break;
        case 3: sendMessage();
                break;
        case 4: modifyTopic();
                break;
        case 5: deleteTopic();
                break;
        case 6: System.exit(1);
                break;
        default: System.out.println("Input error");
      }
    }
  }

  public void createNewTopic(){
    readFile();
    writeFile();
  }

  public void modifyTopic(){
    readFile();
    writeFileModified();
  }

  public void deleteTopic(){
    readFile();
    removeInFile();
  }

  public boolean containsIgnoreCase(List <String> l, String s){
    Iterator <String> it = l.iterator();
    while(it.hasNext()){
      if(it.next().equalsIgnoreCase(s))
      return true;
    }
    return false;
  }

  public void getTopicsAvailable(){
    if(readFile()){
      if(topicsAvailable.size() != 0){
        for(int i = 0; i < topicsAvailable.size(); i++){
          //if(!topicsAvailable.get(i).equals(NEWLINE))
              System.out.println("| " + topicsAvailable.get(i) + " |");
            }
      }else{
        showLines();
        System.out.println("You don't have topics created");
        showLines();
      }
    }
  }

  public void sendMessage() throws Exception{
    readFile();
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

  public void writeFileModified(){
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

              System.out.println("Write the new name:");
              scanner.nextLine();
              String newName = scanner.nextLine();

              topicsAvailable.set(topicSelected - 1, newName);
              //Clear the txt
              PrintWriter pw = new PrintWriter("topics.txt");
              pw.close();
              ByteBuffer buf = ByteBuffer.allocate(48);
              buf.clear();
              for(int i = 0; i < topicsAvailable.size(); i++){
                String output = topicsAvailable.get(i) + NEWLINE;
                buf.put(output.getBytes());
                buf.flip();
                while(buf.hasRemaining()){
                  channel.write(buf);
                }
                buf.clear();
              }
              showLines();
              System.out.println("Topic deleted");
              showLines();
            } else {
              System.out.println("You don't have topics created");
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

  public void removeInFile(){
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
              topicsAvailable.remove(topicsAvailable.get(topicSelected-1));
              PrintWriter pw = new PrintWriter("topics.txt");
              pw.close();
              ByteBuffer buf = ByteBuffer.allocate(48);
              buf.clear();
              for(int i = 0; i < topicsAvailable.size(); i++){
                String output = topicsAvailable.get(i) + NEWLINE;
                buf.put(output.getBytes());
                buf.flip();
                while(buf.hasRemaining()){
                  channel.write(buf);
                }
                buf.clear();
              }
              showLines();
              System.out.println("Topic deleted");
              showLines();
            } else {
              System.out.println("You don't have topics created");
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
      //FileLock lock = channel.lock();
      try{
        //Block the file to avoid other people write or read the file
        FileLock lock = channel.tryLock();
        //lock = channel.tryLock();
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
          /**
          for(int i = 0; i < topicsAvailable.size(); i++){
            System.out.println("Error: " + topicsAvailable.get(i).charAt(0));
          }
          **/
          //eliminateEmptySpaces();
          //lock.release();
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

  public void show(){
    for(int i = 0; i < topicsAvailable.size(); i++){
      System.out.println(topicsAvailable.get(i));
    }
  }

  public void showLines(){
    System.out.println("-----------------------------------------------------");
  }

  public void writeFile(){
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
            System.out.println("Write the name of new topic");
            scanner.nextLine();
            //String newTopic = scanner.nextLine().toLowerCase();
            String input = scanner.nextLine();
            if(containsIgnoreCase(topicsAvailable, input)){
              showLines();
              System.out.println("This topic already exist");
              showLines();
            }else{
              topicsAvailable.add(input);
              //input = NEWLINE+input;
              ByteBuffer buf = ByteBuffer.allocate(48);
              //buf.position(0);
              buf.clear();
              //input += input + "\n";
              //String input = "1. Cine\n";
              //System.out.println(input);
              for(int i = 0; i < topicsAvailable.size(); i++){
                String output = topicsAvailable.get(i) + NEWLINE;
                buf.put(output.getBytes());
                buf.flip();
                while(buf.hasRemaining()){
                  channel.write(buf);
                }
                buf.clear();
              }
              showLines();
              System.out.println("The new topic with name " + input +
                                                          " has been created");
              showLines();
            }
            //lock.release();
//            channel.close();
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
