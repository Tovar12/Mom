1. Run active console:
  1.1. go apache-activemq-5.10.0/bin
  1.2. ./activemq console
2. Compile:
  javac -cp lib/activemq-all-5.10.0.jar EjemploConsumidor.java
  javac -cp lib/activemq-all-5.10.0.jar EjemploProductor.java

3. Run:
  java -cp lib/activemq-all-5.10.0.jar:. EjemploConsumidor
  java -cp lib/activemq-all-5.10.0.jar:. EjemploProductor
