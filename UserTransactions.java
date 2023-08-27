package org.example;

import java.sql.*;
import java.util.Scanner;

public class UserTransactions {
    public int userId;
    public String user;
    public void deposit(){
        int oldamount,newamount,amount,count=0,balance,rowsAffected;
        char choice;
        Scanner input=new Scanner(System.in);
        System.out.println("Enter amount you wish to deposit into your account:");
        amount= input.nextInt();
        //verification of account
        Environment env=new Environment();
        String accountcheck="SELECT `acc_Balance` FROM "+env.usrtbl+" WHERE `acc_No`="+userId+";";
        try{
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(accountcheck);
            while (rs.next()){
                oldamount=rs.getInt(1);
                System.out.println("Your old account balance was: "+oldamount);
                newamount =oldamount+amount;
                System.out.println("Your new account balance will be: "+newamount);
                String deposit="UPDATE "+env.usrtbl+" SET `acc_Balance`= "+newamount+" WHERE `acc_No`="+userId+";";
                PreparedStatement stm = con.prepareStatement(deposit);
                rowsAffected = stm.executeUpdate();
//                System.out.println(rowsAffected);
                if (rowsAffected>0){
                    System.out.println("Amount deposited successfully!");
                    Login ln=new Login();
                    ln.user=user;
                    ln.userId = userId;
                    ln.loginPage();
                }
            }


        }catch(Exception e){ System.out.println(e);}


    }
    public void transact(){
        int account,amount,count=0,balance,rowsAffected;
        char choice;
        Scanner input=new Scanner(System.in);
        System.out.println("Enter the account number you wish to transfer money to:");
        account= input.nextInt();
        //verification of account
        Environment env=new Environment();
        String accountcheck="SELECT `acc_Balance` FROM "+env.usrtbl+" WHERE `acc_No`="+account+";";
        try{
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(accountcheck);
            while (rs.next()){
                count++;
            }
            rs.close();
           // System.out.println("The count: "+count);
            //check if account exists
            if (count==0){
                System.out.println("The account number is invalid. Please enter correct account number");
                System.out.println("Do you wish to enter the account number again? (y/n)");
                choice=input.next().charAt(0);
//                System.out.println("the choice is: "+choice);
                if (choice=='Y'||choice=='y'){
                    UserTransactions ut=new UserTransactions();
                    ut.userId=userId;
                    ut.user=user;
                    ut.transact();
                }
            }
            if (account==userId){
                System.out.println("The account number entered is yours. Please enter correct account number");
                System.out.println("Do you wish to enter the account number again? (y/n)");
                choice=input.next().charAt(0);
//                System.out.println("the choice is: "+choice);
                if (choice=='Y'||choice=='y'){
                    UserTransactions ut=new UserTransactions();
                    ut.userId=userId;
                    ut.user=user;
                    ut.transact();
                }
            }
            if (count==1){
                System.out.println("Enter amount to be transferred from your account");
                amount=input.nextInt();
                String balancecheck="SELECT `acc_Balance` FROM "+env.usrtbl+" WHERE `acc_No`="+userId+";";
                ResultSet rst=stmt.executeQuery(balancecheck);
                while (rst.next()){
                   balance=rst.getInt(1);
                   System.out.println("Your balance is: "+balance);
                   if (balance <= amount){
                        System.out.println("Transaction could not be completed. Insufficient balance.");
                        Login lg=new Login();
                        lg.userId=userId;
                        lg.user=user;
                        lg.loginPage();
                    }
                   if (balance>amount){
                       String date=DateTime.currentdatetime();
                       String message="The transaction is awaiting verification";
                       String inserttransaction="INSERT INTO "+env.transtbl+"(`sender`,`receiver`,amount,verification_Status,initiation_Date,message) VALUES('"+userId+"','"+account+"','"+amount+"','pending','"+date+"','"+message+"');";
                       PreparedStatement stm = con.prepareStatement(inserttransaction);
                       rowsAffected = stm.executeUpdate();
                       //if the entry has not been inserted
                       if (rowsAffected==0){
                           System.out.println("The transaction could not be completed");
                       }
                       //if the entry has  been inserted
                       if (rowsAffected==1){
                           int tid,trowsaffected, tcounter=0;
                           System.out.println("The transaction has been submitted");
                           String transactionfetch="SELECT `trans_ID` FROM "+env.transtbl+" WHERE `sender`="+userId+" AND `receiver` ="+account+" AND `amount`="+amount+" AND`initiation_Date`=TIMESTAMP('"+date+"');";
                           Statement stmt1=con.createStatement();
                           Statement stmt2=con.createStatement();
                           ResultSet trsf=stmt1.executeQuery(transactionfetch);
                           ResultSet trsfc=stmt2.executeQuery(transactionfetch);
                           while (trsfc.next()){
                               tcounter++;
                           }
                           if (tcounter==1){
                               while (trsf.next()){
                                   tid=trsf.getInt(1);
                                   TransactionVerification trv=new TransactionVerification();
                                   trv.user=user;
                                   trv.userId=userId;
                                   trv.initverificationcode(tid,date);

                               }
                           }


                       }
                   }
                }
                rst.close();


            }

        }catch(Exception e){ System.out.println(e);}
    }
    public void transactionHistory(){
        Environment env = new Environment();
        String alltrancactioncheck="SELECT * FROM "+env.transtbl+" WHERE `sender`="+userId+" ORDER BY initiation_Date DESC ;";
        String allrecievedcheck="SELECT * FROM "+env.transtbl+" WHERE `receiver`="+userId+" AND `verification_Status`='completed' ORDER BY initiation_Date DESC ;";
        String pendingtrancactioncheck="SELECT * FROM "+env.transtbl+" WHERE `sender`="+userId+" AND `verification_Status`='pending' ORDER BY initiation_Date DESC;";
        String completedtrancactioncheck="SELECT * FROM "+env.transtbl+" WHERE `sender`="+userId+" AND `verification_Status`='completed' ORDER BY initiation_Date DESC;";
        String flaggedtrancactioncheck="SELECT * FROM "+env.transtbl+" WHERE `sender`="+userId+" AND `verification_Status`='flagged' ORDER BY initiation_Date DESC;";
        String cancelledtrancactioncheck="SELECT * FROM "+env.transtbl+" WHERE `sender`="+userId+" AND `verification_Status`='cancelled' ORDER BY initiation_Date DESC;";
        int code,receiverId,amount,transId;
        String status,date;
        char choice;
        do {
            System.out.println("Within this menu you can check all transaction including the various transaction status, verify or cancel transactions within the categories listed .\n Kindly choose one of the operations below using the assigned code:");
            System.out.println("MENU\n \t Code \t Function \n \t 1. \t Check, verify or cancel all transactions \n \t 2. \t Check, verify or cancel all pending transactions \n \t 3. \t Check all completed transactions \n \t 4. \t Check all cancelled transaction history \n \t 5. \t Check flagged transactions \n \t 6. \t Check received transactions \n \t 7. \t Go back to your homepage \n \t 8. \t Log out of the application \n \t 9. \t Close the program.");
            System.out.println("Enter operation function code:");

            Scanner input=new Scanner(System.in);
            code=input.nextInt();

            switch (code){
                case 1:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(alltrancactioncheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %15s | %12s | %20s | %10s ", "Transaction ID", "Receiver account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                             System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(3),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t Verifying pending transactions \n \t 2. \t Cancel pending transactions \n \t 3. \t View in details selected transaction \n \t 4. \t Go back to your homepage \n \t 5. \t Log out of the application \n \t 6. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to verify from the list above:");
                                    transId=input.nextInt();
                                    TransactionVerification tv = new TransactionVerification();
                                    tv.userId=userId;
                                    tv.user=user;
                                    tv.transID=transId;
                                    tv.finverification();
                                    break;
                                case 2:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to cancel from the list above:");
                                    transId=input.nextInt();
                                    TransactionVerification tvc = new TransactionVerification();
                                    tvc.userId=userId;
                                    tvc.user=user;
                                    tvc.transID=transId;
                                    tvc.canceltransaction();
                                    break;
                                case 3:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 4:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 5:
                                    Main.main(null);
                                    break;
                                case 6:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}


                    break;
                case 2:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(pendingtrancactioncheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %15s | %12s | %20s | %10s ", "Transaction ID", "Receiver account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                            System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(3),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t Verifying pending transactions \n \t 2. \t Cancel pending transactions \n \t 3. \t View in details selected transaction \n \t 4. \t Go back to your homepage \n \t 5. \t Log out of the application \n \t 6. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to verify from the list above:");
                                    transId=input.nextInt();
                                    TransactionVerification tv = new TransactionVerification();
                                    tv.userId=userId;
                                    tv.user=user;
                                    tv.transID=transId;
                                    tv.finverification();
                                    break;
                                case 2:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to cancel from the list above:");
                                    transId=input.nextInt();
                                    TransactionVerification tvc = new TransactionVerification();
                                    tvc.userId=userId;
                                    tvc.user=user;
                                    tvc.transID=transId;
                                    tvc.canceltransaction();
                                    break;
                                case 3:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 4:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 5:
                                    Main.main(null);
                                    break;
                                case 6:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}

                    break;
                case 3:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(completedtrancactioncheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %15s | %12s | %20s | %10s ", "Transaction ID", "Receiver account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                            System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(3),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t View in details selected transaction \n \t 2. \t Go back to your homepage \n \t 3. \t Log out of the application \n \t 4. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 2:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 3:
                                    Main.main(null);
                                    break;
                                case 4:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}

                    break;
                case 4:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(cancelledtrancactioncheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %15s | %12s | %20s | %10s ", "Transaction ID", "Receiver account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                            System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(3),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t View in details selected transaction \n \t 2. \t Go back to your homepage \n \t 3. \t Log out of the application \n \t 4. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 2:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 3:
                                    Main.main(null);
                                    break;
                                case 4:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}
                    break;
                case 5:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(flaggedtrancactioncheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %15s | %12s | %20s | %10s ", "Transaction ID", "Receiver account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                            System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(3),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t View in details selected transaction \n \t 2. \t Go back to your homepage \n \t 3. \t Log out of the application \n \t 4. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 2:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 3:
                                    Main.main(null);
                                    break;
                                case 4:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}
                    break;
                case 6:
                    try {
                        Connection con = DriverManager.getConnection(env.url, env.uname, env.psd);
                        //here Environment Class is used to create an object which contains the database name, username and password

                        Statement stmt = con.createStatement();
                        //check if the transaction is flagged for fraudulent activities
                        ResultSet transactions = stmt.executeQuery(allrecievedcheck);
                        System.out.println("---------------------------------------------------------------------------------------------");
                        System.out.printf("%10s | %16s | %12s | %20s | %10s ", "Transaction ID", "Sender account","Amount sent", "Initiation Date", "Status");
                        System.out.println();
                        System.out.println("---------------------------------------------------------------------------------------------");
                        while (transactions.next()){
                            System.out.format("%14s |%17s |%13s |%21s |%12s ", transactions.getInt(1),transactions.getInt(2),transactions.getInt(4),transactions.getString(6), transactions.getString(5));
                            System.out.println();
                        }
                        System.out.println("----------------------------------------------------------------------------------------------");
                        transactions.close();
                        do{
                            System.out.println("What operations would you like to do?");
                            System.out.println("Kindly choose one of the operations below using the assigned code:");
                            System.out.println("MENU\n \t Code \t Function \n \t 1. \t View in details selected transaction \n \t 2. \t Go back to your homepage \n \t 3. \t Log out of the application \n \t 4. \t Close the program.");
                            System.out.println("Enter operation function code:");
                            code=input.nextInt();

                            switch (code){
                                case 1:
                                    System.out.println("Kindly enter transaction ID for the transaction you want to view from the list above:");
                                    transId=input.nextInt();
                                    String viewtransactioncheck="SELECT * FROM "+env.transtbl+" WHERE `trans_ID`="+transId+";";
                                    ResultSet trschk=stmt.executeQuery(viewtransactioncheck);
                                    trschk.next();
                                    receiverId=trschk.getInt(3);
                                    amount=trschk.getInt(4);
                                    String transdate=trschk.getString(6);
                                    String  verstatus=trschk.getString(5);
                                    String message=trschk.getString(7);
                                    String[] splitStr = transdate.split("\\s+");
//                                    System.out.println(splitStr[0]);
//                                    System.out.println(splitStr[1]);
                                    System.out.println("The recepient account number: "+receiverId);
                                    System.out.println("The amount: "+amount);
                                    System.out.println("The transaction was initiated on: "+splitStr[0]);
                                    System.out.println("The transaction was initiated at: "+splitStr[1]);
                                    System.out.println("The transaction status: "+verstatus);
                                    System.out.println("The transaction message: "+message);
                                    System.out.println("Redirecting you to your homepage.");
                                    break;
                                case 2:
                                    Login ln =new Login();
                                    ln.user=user;
                                    ln.userId=userId;
                                    ln.loginPage();
                                    break;
                                case 3:
                                    Main.main(null);
                                    break;
                                case 4:
                                    System.out.println("Do you wish to close the application? (y/n)");
                                    choice=input.next().charAt(0);
                                    if (choice=='Y'||choice=='y') {
                                        System.exit(0);
                                    }else {
                                        Login lgn =new Login();
                                        lgn.user=user;
                                        lgn.userId=userId;
                                        lgn.loginPage();
                                    }
                                    break;
                                default:
                                    System.out.println("Kindly enter the correct operation function code:");
                                    break;
                            }
                            System.out.println("Do you wish to continue?");
                            choice=input.next().charAt(0);
                        } while (choice=='Y'||choice=='y');


                    }catch (Exception e){System.out.println(e);}
                    break;
                case 7:
                    Login ln =new Login();
                    ln.user=user;
                    ln.userId=userId;
                    ln.loginPage();
                    break;
                case 8:
                    Main mn=new Main();
                    mn.main(null);
                    break;
                case 9:
                    System.out.println("Do you wish to close the application? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y') {
                        System.exit(0);
                    }else {
                        Login lgn =new Login();
                        lgn.user=user;
                        lgn.userId=userId;
                        lgn.loginPage();
                    }
                    break;
                default:
                    System.out.println("Kindly enter the correct operation function code:");
                    break;
            }
            System.out.println("Do you wish to continue? (y/n)");
            choice=input.next().charAt(0);
        } while (choice=='Y'||choice=='y');
    }
}
