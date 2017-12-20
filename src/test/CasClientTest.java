package test;

import java.io.File;

import org.apache.log4j.BasicConfigurator;

import bzh.plealog.blastviewer.util.CasClient;

/**
 * This is a CAS authentication client test application. 
 * 
 * @author Patrick G. Durand
 */
public class CasClientTest {
 
  
  private static void test1(CasClient client, String service, String serviceTicket) {
    String answer=client.getServiceCallAsString(service, serviceTicket);
    if(answer!=null) {
      int size = answer.length();
      System.out.println("Answer (1k): \n\n" +answer.substring(0, Math.min(1024, size)));
      System.out.println("\n Total: "+size+" bytes");
    }
  }
  private static void test2(CasClient client, String service, String serviceTicket) {
    File answer=client.getServiceCallAsFile(service, serviceTicket);
    if(answer!=null) {
      System.out.println("File: " +answer.getAbsolutePath());
      System.out.println("  /!\\ do not forget to delete it!");
    }
  }

  public static void main(String[] args) {
    BasicConfigurator.configure();
    
    if (args.length!=4) {
      System.err.println("Use: CasClientTest   cas-rest-api-server-url   username   password   service-url");
      System.exit(1);
    }
    String server = args[0];
    String username = args[1];
    String password = args[2];
    String service = args[3];
    
    CasClient client = new CasClient();
    
    String ticketGrantingTicket = client.getTicketGrantingTicket(server, username, password);
    if (ticketGrantingTicket!=null) {
      System.out.println("TicketGrantingTicket is:" + ticketGrantingTicket);
      String serviceTicket = client.getServiceTicket(server, ticketGrantingTicket, service);
      if (serviceTicket!=null) {
        System.out.println("ServiceTicket is:" + serviceTicket);
        test1(client, service, serviceTicket);
      }
      serviceTicket = client.getServiceTicket(server, ticketGrantingTicket, service);
      if (serviceTicket!=null) {
        System.out.println("ServiceTicket is:" + serviceTicket);
        test2(client, service, serviceTicket);
      }
    }
    client.logout(server, ticketGrantingTicket);
  }

}
