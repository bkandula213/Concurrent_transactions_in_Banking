package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
public class CreateUser {
    //user details
    public String name;
    public int phone;
    public String email;
    public String psd;
    public int deposit;
    private int resultcounter=0;
    private String psdenc;
    int rowsAffected;

    //to check if user email exists
    public void userCheck(String cname,String cemail,int cdepo,int cnumber,String cpass){
//        System.out.println(cname+" "+cnumber+" "+cemail+" "+cpass+" "+cdepo);
        try{
            Environment env=new Environment();
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            String query ="select * from "+env.usrtbl+" where `acc_Email` = '"+cemail+"';";
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()){
                resultcounter++;
            }
            rs.close();
//                System.out.println(rs.getString(1)+"  "+rs.getString(2)+"  "+rs.getString(3)+" "+rs.getString(4));

            //con.close();
            if (resultcounter==0){
                System.out.println(resultcounter);
                try {
                    Environment envin=new Environment();
                    Connection conn= DriverManager.getConnection(env.url,env.uname, env.psd);
                    //here Environment Class is used to create an object which contains the database name, username and password
                    Statement stmts=con.createStatement();
                    SHA512 enc=new SHA512();
                    psdenc=enc.encryptThisString(cpass);
                    //System.out.println(psdenc);
                    String insert="INSERT INTO "+env.usrtbl+"(acc_HolderName,acc_Balance,acc_PhoneNo,acc_Email,acc_Password) VALUES('"+cname+"','"+cdepo+"','"+cnumber+"','"+cemail+"','"+psdenc+"');";
                    PreparedStatement stm = conn.prepareStatement(insert);
                    rowsAffected = stm.executeUpdate();
                    System.out.println(rowsAffected);
                    if (rowsAffected>0){
                        String accquery ="select `acc_No` from "+env.usrtbl+" where `acc_Email` = '"+cemail+"';";
                        ResultSet accrs=stmt.executeQuery(accquery);
                        accrs.next();
                        int acc_No= accrs.getInt(1);
                        String datetime=DateTime.currentdatetime();
                        String[] splitStr = datetime.split("\\s+");
                        String date=splitStr[0];
                        String time =splitStr[1];
                        String subject="Welcome to SB Bank.";
                        String welcomemessage= "Welcome to SB Bank. Your account number is: <div style=\"color:#41B3A3;\"><h1>"+acc_No+"</h1></div> <br> the account was made on: "+date+" at: "+time+". To get started, log in to your account to transact your money. <br> <br> Best regards, <br> <br> SB Bank team.";
                        SendMail SC =new SendMail();
                        SC.VerificationMailersender(cemail,subject,welcomemessage);
                        System.out.println("The confirmation email has been sent.");
                        System.out.println("Account created successfully. Proceed to log in to your account");
                        Main mn =new Main();
//                        DBConnect db = new DBConnect();
//                        db.Connect();
                        mn.login();
                    }
                    con.close();
                    conn.close();



                }catch (Exception e){ System.out.println(e);}
            }else {
                System.out.println("Email already exists. Kindly use another email address");
                Main mn =new Main();
                mn.signup();
            }
        }catch(Exception e){ System.out.println(e);}
    }
}
