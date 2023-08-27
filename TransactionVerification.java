package org.example;

import org.checkerframework.checker.i18nformatter.qual.I18nFormat;

import java.sql.*;
import java.util.Scanner;

public class TransactionVerification {
    public int userId;
    public String user;
    public int transID;
    private String flag="flagged";
    private final String pending="pending";
    private String verified="verified";
    private String cancelled="cancelled";
    private String valid="Valid";
    private String  invalid="Invalid";
    private String completed="completed";
    public void initverificationcode(int tID,String curdt){
        int rowsAffected,tid=tID;
        String date=curdt;
        Environment env=new Environment();
        String vrfcode=RandomNumber.getRandomNumberString();
        String expdate=DateTime.expdatetime();
//        System.out.println("The trs code: "+tid);
//        System.out.println("The vrf code: "+vrfcode);
//        System.out.println("The current datetime: "+date);
        String insertverification="INSERT INTO "+env.vertbl+"(trans_ID,verification_Code,request_Date,request_TTD,code_Status) VALUES('"+tid+"','"+vrfcode+"','"+date+"','"+expdate+"','"+valid+"');";
        try {
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password
            PreparedStatement vrfstm = con.prepareStatement(insertverification);
            rowsAffected = vrfstm.executeUpdate(insertverification);
            if (rowsAffected==0){
                System.out.println("Code insert failed");
            }
            if (rowsAffected==1){
                System.out.println("Verification code sent");
                String subject,message,email;
                String emailcheck="SELECT `acc_Email` FROM "+env.usrtbl+" WHERE `acc_No`="+userId+";";
                Statement stmt=con.createStatement();
                ResultSet rst=stmt.executeQuery(emailcheck);
                while (rst.next()){
                    email=rst.getString(1);
                    subject="Verification code for transaction ID: "+tid+" made at: "+date;
                    message= "The verification code for the transaction: "+tid+" made on "+date+" is: <br><div style=\"margin-left: 30px; color:#41B3A3;\"><h1>"+vrfcode+"</h1></div> <br> It expires after 15 minutes at "+expdate+".<br> If you did not make the transaction log in to your account, go to check transaction history, select cancel transaction and enter transaction code to cancel.";
                    SendMail SC=new SendMail();
                    SC.transID =tid;
                    SC.user=user;
                    SC.userId=userId;
                    SC.TransferMailersender(email,subject,message);

                }
                rst.close();

            }
        }catch (Exception e){ System.out.println(e);}

    }
    public void finverification(){
        int rowsAffected,sender,receiver,amount,verid, validitycheck, flaggedtrans=0,validcodescounter=0;
        String vercode,verstatus,transdate,fetchedcode,fetchedexpdate,currentdatetime;
        char choice;
//        System.out.println("We are now here");
//        System.out.println(transID);
        Scanner input=new Scanner(System.in);
        Environment env=new Environment();
        String acceptmessage="The transaction is completed";
        String verificationmessage="The transaction is completed";
        String flaggedmessage="The transaction has been flagged as suspicious. It seems that there is a transaction made at the same time which violates the transaction process.";
        String transactioncheck="SELECT `sender`,`receiver`,`amount`,`initiation_Date`,`verification_Status` FROM "+env.transtbl+" WHERE `trans_ID`="+transID+";";
        String flaggedtransactioncheck="SELECT `trans_id` FROM "+env.fltrtbl+" WHERE `trans_ID`="+transID+";";
        String verificationcodecheck="SELECT * FROM "+env.vertbl+" WHERE `trans_ID`="+transID+" AND `code_Status`='"+valid+"' ORDER BY request_TTD DESC;";
        String updateflaggedtransaction="UPDATE "+env.transtbl+" SET `verification_Status`='"+flag+"',`message`= '"+flaggedmessage+"' WHERE `trans_ID`="+transID+";";
        String updateverifiedtransaction="UPDATE "+env.transtbl+" SET `verification_Status`='"+verified+"',`message`='"+verificationmessage+"' WHERE `trans_ID`="+transID+";";
        String updatecompletedtransaction="UPDATE "+env.transtbl+" SET `verification_Status`='"+completed+"',`message`= '"+acceptmessage+"' WHERE `trans_ID`="+transID+";";

        try {
            Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password

            Statement stmt=con.createStatement();
            //check if the transaction is flagged for fraudulent activities
            ResultSet flgtrschk=stmt.executeQuery(flaggedtransactioncheck);
            while (flgtrschk.next()){
                flaggedtrans++;
            }
//            System.out.println("Flagged transaction count:" +flaggedtrans);
            flgtrschk.close();
            if (flaggedtrans>0){
                //when a transaction is found in the flagged table
                PreparedStatement stm = con.prepareStatement(updateflaggedtransaction);
                rowsAffected = stm.executeUpdate();
                if (rowsAffected>0){
                    System.out.println(flaggedmessage);
                }
                stm.close();
            }
            if (flaggedtrans==0){
                //prepare variables to be used
//                Statement stmt=con.createStatement();
                //check if the transaction is flagged for fraudulent activities
                ResultSet trschk=stmt.executeQuery(transactioncheck);
                trschk.next();
                sender=trschk.getInt(1);
                receiver=trschk.getInt(2);
                amount=trschk.getInt(3);
                transdate=trschk.getString(4);
                verstatus=trschk.getString(5);
//                System.out.println("The sender id: "+sender);
//                System.out.println("The receiver id: "+receiver);
//                System.out.println("The  amount: "+amount);
//                System.out.println("The  status: "+verstatus);
                //check if the status is flagged
                if (verstatus.equals(flag)){
                    System.out.println(flaggedmessage);
                    System.out.println("Do you wish to wish to verify another transaction? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y'){
                        UserTransactions ut=new UserTransactions();
                        ut.user=user;
                        ut.userId=userId;
                        ut.transactionHistory();
                    }
                }
                //check if the status is cancelled
                if (verstatus.equals(cancelled)){
                    String  cancelledmessage="The transaction was cancelled by the user.";
                    System.out.println(cancelledmessage);
                    System.out.println("Do you wish to wish to verify another transaction? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y'){
                        UserTransactions ut=new UserTransactions();
                        ut.user=user;
                        ut.userId=userId;
                        ut.transactionHistory();
                    }
                }
                //check if the status is completed
                if (verstatus.equals(completed)){
                    String  completedmessage="The transaction was completed by the user.";
                    System.out.println(completedmessage);
                    System.out.println("Do you wish to wish to verify another transaction? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y'){
                        UserTransactions ut=new UserTransactions();
                        ut.user=user;
                        ut.userId=userId;
                        ut.transactionHistory();
                    }else {
                        System.out.println("Redirecting you to the homepage");
                        Login ln=new Login();
                        ln.user=user;
                        ln.userId=userId;
                        ln.loginPage();
                    }
                }
                //check if the status is pending
                if (verstatus.equals(pending)){

                    //count valid codes
                    ResultSet vercodectchk=stmt.executeQuery(verificationcodecheck);
                    while (vercodectchk.next()){
                        validcodescounter++;
                    }
                    vercodectchk.close();
                    //there is no valid code
                    if (validcodescounter==0){
                        System.out.println("Do you wish to resend a verification code? (y/n)");
                        choice=input.next().charAt(0);
                        if (choice=='Y'||choice=='y'){
                           initverificationcode(transID,transdate);
                        }else {
                            System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                            Login ln=new Login();
                            ln.user=user;
                            ln.userId=userId;
                            ln.loginPage();
                        }
                    }
                    //if there is a valid code
                    if (validcodescounter>0){
                        ResultSet vercodefetch=stmt.executeQuery(verificationcodecheck);
                        vercodefetch.next();
                        verid=vercodefetch.getInt(1);
                        fetchedcode=vercodefetch.getString(3);
                        fetchedexpdate=vercodefetch.getString(5);
                        currentdatetime=DateTime.currentdatetime();
                        validitycheck=expiry(currentdatetime,fetchedexpdate);
//                        System.out.println("The most recent transaction code: "+fetchedcode+" as of: "+fetchedexpdate);
                        if (validitycheck==0){
                            //invalid code
                            System.out.println("The most recent verification code is going to be invalid in the next second.");
                            String invalidverificationcode="UPDATE "+env.vertbl+" SET `code_Status`= '"+invalid+"' WHERE `verification_ID`="+verid+";";
                            PreparedStatement invstm = con.prepareStatement(invalidverificationcode);
                            rowsAffected = invstm.executeUpdate();
                            if(rowsAffected>0){
                                System.out.println("Do you wish to resend a verification code? (y/n)");
                                choice=input.next().charAt(0);
                                if (choice=='Y'||choice=='y'){
                                    initverificationcode(transID,transdate);
                                }else {
                                    System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                                    Login ln=new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                }
                            }
                        }
                        if (validitycheck>0){
                            //invalid code
                            System.out.println("The most recent verification code is invalid");
                            String invalidverificationcode="UPDATE "+env.vertbl+" SET `code_Status`= '"+invalid+"' WHERE `verification_ID`="+verid+";";
                            PreparedStatement invstm = con.prepareStatement(invalidverificationcode);
                            rowsAffected = invstm.executeUpdate();
                            if(rowsAffected>0){
                                System.out.println("Do you wish to resend a verification code? (y/n)");
                                choice=input.next().charAt(0);
                                if (choice=='Y'||choice=='y'){
                                    initverificationcode(transID,transdate);
                                }else {
                                    System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                                    Login ln=new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                }
                            }
                        }
                        if (validitycheck<0){
//                            System.out.println("The most verification code is still valid");
                            //ask for recent code from user
                            System.out.println("Kindly enter the most recent verification code for transaction ID: "+transID+" sent to your email:");
                            vercode=input.next();
//                            System.out.println("\n");
                            //valid code
                            String myverificationcodecheck="SELECT * FROM "+env.vertbl+" WHERE `trans_ID`="+transID+" AND `verification_Code`='"+vercode+"' ORDER BY request_TTD DESC;";
                            Statement mystmt=con.createStatement();
                            ResultSet myvercodefetch=mystmt.executeQuery(myverificationcodecheck);
                            myvercodefetch.next();
                            int myverid=vercodefetch.getInt(1);
                            String myfetchedcode=vercodefetch.getString(3);
                            String myfetchedexpdate=vercodefetch.getString(5);
                            String mycurrentdatetime=DateTime.currentdatetime();
                            int myvaliditycheck=expiry(mycurrentdatetime,myfetchedexpdate);
                            if (myvaliditycheck==0){
                                System.out.println("Your verification code is going to be invalid in the next second.");
                                //invalid code
                                String invalidverificationcode="UPDATE "+env.vertbl+" SET `code_Status`= '"+invalid+"' WHERE `verification_Code`='"+vercode+"';";
                                PreparedStatement invstm = con.prepareStatement(invalidverificationcode);
                                rowsAffected = invstm.executeUpdate();
                                if(rowsAffected>0){
                                    System.out.println("Do you wish to resend a verification code? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y'){
                                        initverificationcode(transID,transdate);
                                    }else {
                                        System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                                        Login ln=new Login();
                                        ln.user=user;
                                        ln.userId=userId;
                                        ln.loginPage();
                                    }
                                }
                            }
                            if (myvaliditycheck>0){
                                //invalid code
                                System.out.println("Your verification code is invalid");
                                String invalidverificationcode="UPDATE "+env.vertbl+" SET `code_Status`= '"+invalid+"' WHERE `verification_Code`='"+vercode+"';";
                                PreparedStatement invstm = con.prepareStatement(invalidverificationcode);
                                rowsAffected = invstm.executeUpdate();
                                if(rowsAffected>0){
                                    System.out.println("Do you wish to resend a verification code? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y'){
                                        initverificationcode(transID,transdate);
                                    }else {
                                        System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                                        Login ln=new Login();
                                        ln.user=user;
                                        ln.userId=userId;
                                        ln.loginPage();
                                    }
                                }
                            }
                            if (myvaliditycheck<0){
                                System.out.println("Your verification code is valid");
                                if (vercode.equals(fetchedcode)){
                                    //execute steps to process transaction
                                    //first get old balances
                                    int oldsenderbalance,oldrecepientbalance,newsenderbalance,newrecepientbalance,sendercalcbalance,recepientcalcblance;
                                    String sendermail,receivermail;
                                    String sendbal="SELECT `acc_Balance`,`acc_Email` FROM "+env.usrtbl+" WHERE `acc_No`="+sender+";";
                                    String recbal="SELECT `acc_Balance`,`acc_Email` FROM "+env.usrtbl+" WHERE `acc_No`="+receiver+";";
                                    Statement myinitstmtsend=con.createStatement();
                                    ResultSet initsenderbal =myinitstmtsend.executeQuery(sendbal);
                                    initsenderbal.next();
                                    oldsenderbalance=initsenderbal.getInt(1);
                                    sendermail=initsenderbal.getString(2);
                                    initsenderbal.close();
                                    myinitstmtsend.close();
                                    Statement myinitstmtrec=con.createStatement();
                                    ResultSet initrecbal = myinitstmtrec.executeQuery(recbal);
                                    initrecbal.next();
                                    oldrecepientbalance=initrecbal.getInt(1);
                                    receivermail=initrecbal.getString(2);
                                    initrecbal.close();
                                    myinitstmtrec.close();
                                    sendercalcbalance = oldsenderbalance-amount;
                                    recepientcalcblance=oldrecepientbalance+amount;
                                    PreparedStatement mystm = con.prepareStatement(updateverifiedtransaction);
                                    rowsAffected = mystm.executeUpdate();
                                    if (rowsAffected==1){
                                        System.out.println("The transaction has been verified. Awaiting completion of transaction");
                                        Statement myfinstmtsend=con.createStatement();
                                        ResultSet finsenderbal =myfinstmtsend.executeQuery(sendbal);
                                        finsenderbal.next();
                                        newsenderbalance=finsenderbal.getInt(1);
                                        myfinstmtsend.close();
                                        Statement myfinstmtrec=con.createStatement();
                                        ResultSet finrecbal = myfinstmtrec.executeQuery(recbal);
                                        finrecbal.next();
                                        newrecepientbalance=finrecbal.getInt(1);
                                        initrecbal.close();
                                        myinitstmtrec.close();
                                        if (newsenderbalance==sendercalcbalance && newrecepientbalance==recepientcalcblance){
                                            PreparedStatement myverstm = con.prepareStatement(updatecompletedtransaction);
                                            rowsAffected = myverstm.executeUpdate();
                                            if (rowsAffected==1) {
                                                System.out.println("The transaction has been completed.");
                                                SendMail SC = new SendMail();
                                                SC.user=user;
                                                SC.userId=userId;
                                                String sendersubject="Confirmation for transaction ID: "+transID+" made at: "+transdate;
                                                String sendermessage= "The transaction: "+transID+" made on "+transdate+" of "+amount+" is successful. Your new account balance is: <div style=\"color:#41B3A3;\">"+newsenderbalance+"</div> <br>";
                                                String receiversubject="New transaction made at: "+transdate;
                                                String receivermessage= "You have received "+amount+" from acc. no.: "+sender+" made on "+transdate+" . Your new account balance is: <div style=\"color:#41B3A3;\">"+newrecepientbalance+"</div> <br>";
                                                SC.VerificationMailersender(sendermail,sendersubject,sendermessage);
                                                SC.VerificationMailersender(receivermail,receiversubject,receivermessage);
                                                System.out.println("The email has been sent. Redirecting you to your homepage.");
                                                Login ln =new Login();
                                                ln.user=user;
                                                ln.userId=userId;
                                                ln.loginPage();

                                            }
                                            myverstm.close();
                                        }



                                    }
                                    mystm.close();
                                }else {
                                    System.out.println("The code you entered is not the most recent. Do you wish to enter another verification code? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y'){
                                        finverification();
                                    }else {
                                        System.out.println("The transaction verification was aborted. Redirecting you to the homepage");
                                        Login ln=new Login();
                                        ln.user=user;
                                        ln.userId=userId;
                                        ln.loginPage();
                                    }
                                }
                            }
                            myvercodefetch.close();

                        }

                    }
                }
                trschk.close();

            }
            stmt.close();

            con.close();
        }catch (Exception e){System.out.println(e);}
    }
    private int expiry(String cur,String exp){

        Timestamp exptime= Timestamp.valueOf(exp);
        Timestamp curtime= Timestamp.valueOf(cur);
        int validityres=curtime.compareTo(exptime);
        return validityres;
    }
    public void canceltransaction(){
        int rowsAffected=0;
        char choice;
        System.out.println("We are now here");
        System.out.println(transID);
        Scanner input=new Scanner(System.in);
        Environment env=new Environment();
        String cancelmessage="The transaction was terminated by the user.";
        String updatecancelledtransaction="UPDATE "+env.transtbl+" SET `verification_Status`='"+cancelled+"',`message`= '"+cancelmessage+"' WHERE `trans_ID`="+transID+";";
        System.out.println("Are you sure you want to cancel the transaction? (y/n)");
        choice=input.next().charAt(0);
        if (choice=='Y'||choice=='y'){
            try {
                Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                //here Environment Class is used to create an object which contains the database name, username and password
                PreparedStatement mycancstm = con.prepareStatement(updatecancelledtransaction);
                rowsAffected = mycancstm.executeUpdate();
                if (rowsAffected==1) {
                    System.out.println("The transaction has been cancelled.");
                    Login ln=new Login();
                    ln.user=user;
                    ln.userId=userId;
                    ln.loginPage();
                }else {
                    System.out.println("The transaction cancel has failed. Try again later.");
                    Login ln=new Login();
                    ln.user=user;
                    ln.userId=userId;
                    ln.loginPage();
                }
                mycancstm.close();
                con.close();
            }catch (Exception e){System.out.println(e);}
        }else{
            System.out.println("You will be redirected to your homepage.");
            Login ln=new Login();
            ln.user=user;
            ln.userId=userId;
            ln.loginPage();
        }
    }
}
