package org.example;
import java.util.Scanner;
class Main{
    private String uname;
    private String uemail;
    private int phone;
    private int acc;
    private String pswd;
    private int dep;

    public void signup(){
        Main mn = new Main();
//        DBConnect db = new DBConnect();
//        db.Connect();
        Scanner input =new Scanner(System.in);
        CreateUser cr = new CreateUser();
        String psd ,psdchk,name,email;
        int depo, number;
        System.out.println("Kindly enter your name:");
        name=input.next();
        cr.name=name;
        System.out.println("Kindly enter deposit amount:");
        depo=input.nextInt();
        cr.deposit= depo;
        System.out.println("Kindly enter your email:");
        email=input.next();
        cr.email=email;
        System.out.println("Kindly enter your phone number:");
        number=input.nextInt();
        cr.phone=number;
        do {
            System.out.println("Kindly enter your Password:");
            psd=input.next();
            System.out.println("Kindly repeat your password:");
            psdchk=input.next();
            if (psd.equals(psdchk)){
                //break;
                mn.pswd=psd;
                cr.psd=psd;
            } else {
                System.out.println("The password check does not match with the repeated password. Repeat the password process");
            }
        }while (!psd.equals(psdchk));
//        cr.psd=mn.pswd;
//        System.out.println("name:"+mn.uname+" \nEmail: "+mn.uemail+"\nPhone Number: "+mn.phone+"\nPassword: "+psd+"\nPassword check: "+psdchk);
        cr.userCheck(name,email,depo,number,psd);
    }
    public void login(){
        Main mn = new Main();
        Scanner input =new Scanner(System.in);
//        System.out.println("Kindly enter your account number:");
//        mn.acc=input.nextInt();
        System.out.println("Kindly enter your email:");
        mn.uemail=input.next();
        System.out.println("Kindly enter your Password:");
        mn.pswd=input.next();
//        System.out.println("\nAccount: "+mn.acc+"\nEmail: "+mn.uemail+"\nPassword: "+mn.pswd);
        Login ln=new Login();
        ln.vaildityCheck(mn.uemail,mn.pswd);
    }

    public static void main(String args[]){
//        DBConnect db = new DBConnect();
//        db.Connect();
        char choice;
        do{
            Main mn = new Main();
            CreateUser cr = new CreateUser();
            Scanner input =new Scanner(System.in);
            int code;

            String psd ,psdchk;
            System.out.println("Welcome to SB Bank");
            System.out.println("Kindly choose one of the two options below using the assigned code:");
            System.out.println("MENU\n \t Code \t Function \n \t 1. \t Create a new account (if you don't have one) \n \t 2. \t Log in to your account \n \t 3. \t Exit the application");
            System.out.println("Enter operation function code:");
            code=input.nextInt();
            //do while loop
            //do {
            //switch code
            switch (code){
                case 1:
                    mn.signup();
                    break;
                case 2:
                    mn.login();
                    break;
                case 3:
                    System.out.println("Do you wish to close the application? (y/n)");
                    choice=input.next().charAt(0);
                    if (choice=='Y'||choice=='y') {
                        System.exit(0);
                    }else {
                        Main.main(null);
                    }
                default:
                    System.out.println("Kindly enter the correct operation function code:");
            }
            System.out.println("Do you wish to continue? (y/n)");
            choice=input.next().charAt(0);
        } while (choice=='Y'||choice=='y');


    }
}