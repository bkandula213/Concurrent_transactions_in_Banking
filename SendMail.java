package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.Scanner;

public class SendMail {
    private final String FROM = "noreply.test.SBBank@gmail.com";
    String from = "noreply@sbbank.com";
    public String user;
    public int transID;
    public int userId;

    private final Gmail service;
    public SendMail() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory JsonFactory = GsonFactory.getDefaultInstance();
        service = new Gmail.Builder(HTTP_TRANSPORT, JsonFactory, getCredentials(HTTP_TRANSPORT, JsonFactory))
                .setApplicationName("Test mailer")
                .build();
    }


    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, GsonFactory JsonFactory)
            throws IOException {
        // Load client secrets.
        InputStream in = SendMail.class.getResourceAsStream("/client_secret_543815666552-e2hvkvb569mulrsj31kbbdarcure8rfr.apps.googleusercontent.com.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: ");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JsonFactory, new InputStreamReader(SendMail.class.getResourceAsStream("/client_secret_543815666552-e2hvkvb569mulrsj31kbbdarcure8rfr.apps.googleusercontent.com.json")));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JsonFactory, clientSecrets, Set.of(GmailScopes.GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }
    public  void TransferMailersender(String address,String subject,String msg) throws IOException,  MessagingException {

        // Create the email content
        String messageSubject = subject;
        String bodyText = msg;
        String emailAddress=address;
        String bankemail="noreply.test.SBBank@gmail.com";

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(emailAddress));
        email.setSubject(messageSubject);
        email.setText(bodyText,"utf-8","html");


        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create the draft message
//            Draft draft = new Draft();
//            draft.setMessage(message);
            message= service.users().messages().send("me", message).execute();
//            System.out.println("Message id: " + message.getId());
//            System.out.println(message.toPrettyString());
            Environment env=new Environment();
            String emailcheck="SELECT `acc_Email` FROM "+env.usrtbl+" WHERE `acc_No`="+transID+";";

            char choice;
            Scanner input=new Scanner(System.in);
            System.out.println("Do you want to verify the transaction? (y/n)");
            choice=input.next().charAt(0);
            if (choice=='Y'||choice=='y'){
                TransactionVerification tv=new TransactionVerification();
                tv.transID=transID;
                tv.user=user;
                tv.userId=userId;
                tv.finverification();
            }

            if (choice!='Y'){
                Login ln = new Login();
                ln.userId=userId;
                ln.user=user;
                ln.loginPage();
            }if (choice!='y'){
                Login ln = new Login();
                ln.userId=userId;
                ln.user=user;
                ln.loginPage();
            }


        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to create Message: " + e.getDetails());
            } else {
                throw e;
            }
        }
    }
    public  void VerificationMailersender(String address,String subject,String msg) throws IOException,  MessagingException {

        // Create the email content
        String messageSubject = subject;
        String bodyText = msg;
        String emailAddress=address;
        String bankemail="noreply.test.SBBank@gmail.com";

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(emailAddress));
        email.setSubject(messageSubject);
        email.setText(bodyText,"utf-8","html");


        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create the draft message
//            Draft draft = new Draft();
//            draft.setMessage(message);
            message= service.users().messages().send("me", message).execute();
//            System.out.println("Message id: " + message.getId());
//            System.out.println(message.toPrettyString());



        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to create Message: " + e.getDetails());
            } else {
                throw e;
            }
        }
    }

}

