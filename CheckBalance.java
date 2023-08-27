package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckBalance {
    public String user;
    public void Balancecheck(int userid){
        Environment env=new Environment();
        String balancequery="SELECT `acc_Balance` FROM "+env.usrtbl+" WHERE `acc_No`="+userid+";";
        try {
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Class is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(balancequery);
            LocalDateTime currentdatetime = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formatteddate = currentdatetime.format(myFormatObj);
            rs.next();
            System.out.println("Your account balance as of "+formatteddate+" is: "+rs.getInt(1));

            con.close();
            Login ln=new Login();
            ln.user=user;
            ln.userId = userid;
            ln.loginPage();


        }catch(Exception e){ System.out.println(e);}
    }
}
