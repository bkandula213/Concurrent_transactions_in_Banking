package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnect {
    public void Connect () {
        try{
            Environment env=new Environment();
            Connection con= DriverManager.getConnection(env.url,env.uname, env.psd);
            //here Environment Clas is used to create an object which contains the database name, username and password
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from "+env.usrtbl);
            System.out.println("Account Number\tAccount holder\tAccount balance\tPhone number\tEmail\tPassword");
            while(rs.next())
                System.out.println(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t0"+rs.getString(4)+"\t"+rs.getString(5)+"\t"+rs.getString(6));

            con.close();

        }catch(Exception e){ System.out.println(e);}
    }
}
