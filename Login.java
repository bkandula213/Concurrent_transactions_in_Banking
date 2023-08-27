package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Login {
    private String username;
    private String fetchedPassword;
    public int userId;
    private String email;
    public String user;
    public void vaildityCheck(String umail,String psd){
        Main mn=new Main();
        SHA512 sh=new SHA512();
        int resultcounter=0;
        Environment env=new Environment();
        String passwordHash= sh.encryptThisString(psd);
        String useraccount="SELECT * FROM "+env.usrtbl+" WHERE `acc_Email`='"+umail+"';";
        try {
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Clas is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            Statement stmts=con.createStatement();
            ResultSet rs=stmt.executeQuery(useraccount);
            ResultSet rsc=stmts.executeQuery(useraccount);
            while(rsc.next()){
                resultcounter++;
            }
            //System.out.println(resultcounter);
            if (resultcounter==0){
                System.out.println("Email not found. Kindly try again");
                mn.login();
            }
            if (resultcounter==1){
                    rs.next();
                    username=rs.getString(2);
                    fetchedPassword=rs.getString(6);
                    userId=rs.getInt(1);
                    user=username;


                //System.out.println(userId+" "+username+" "+fetchedPassword);

            }
            con.close();


        }catch(Exception e){ System.out.println(e);}
        if (passwordHash.equals(fetchedPassword)){
            loginPage();
        }else {
            System.out.println("Wrong password. Kindly log in again");
            mn.login();
        }
    }
    public void loginPage(){
        int code;
        char choice;
        do {
            System.out.println("Welcome to your account "+user);
            System.out.println("Kindly choose one of the operations below using the assigned code:");
            System.out.println("MENU\n \t Code \t Function \n \t 1. \t Check Account Balance \n \t 2. \t Make a deposit \n \t 3. \t Make a transaction \n \t 4. \t Check your transaction history \n \t 5. \t Log out of your account\n \t 6. \t Close the program");
            System.out.println("Enter operation function code:");

            Scanner input=new Scanner(System.in);
            CheckBalance cb =new CheckBalance();
            UserTransactions ut=new UserTransactions();
            code=input.nextInt();

            switch (code){
                case 1:
                    cb.user=user;
                    cb.Balancecheck(userId);
                    break;
                case 2:
                    ut.userId=userId;
                    ut.user=user;
                    ut.deposit();
                    break;
                case 3:
                    ut.userId=userId;
                    ut.user=user;
                    ut.transact();
                    break;
                case 4:
                    ut.userId=userId;
                    ut.user=user;
                    ut.transactionHistory();
                    break;
                case 5:
                    Main mn=new Main();
                    mn.main(null);
                    break;
                case 6:
                    System.out.println("Do you wish to close the application? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y') {
                        System.exit(0);
                    }else {
                            loginPage();
                    }
                    break;
                default:
                    System.out.println("Kindly enter the correct operation function code:");
                    break;
            }
            System.out.println("Do you wish to continue?");
            choice=input.next().charAt(0);
        } while (choice=='Y'||choice=='y');
    }
}
