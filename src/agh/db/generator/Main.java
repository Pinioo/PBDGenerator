/*
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    String url = "jdbc:sqlserver://localhost;user=jakpin;password=123";
    Connection con = null;
    con = DriverManager.getConnection(url);
    Statement st = con.createStatement();
    st.execute(Conference.randConferenceSQL());
    con.close();
*/

package agh.db.generator;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.microsoft.sqlserver.jdbc.SQLServerResource_zh_CN;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            Scanner in = new Scanner(System.in);
            System.out.print("Database IP Address: ");
            String ipadd = in.nextLine();

            System.out.print("Username: ");
            String username = in.nextLine();

            System.out.print("Password: ");
            String password = in.nextLine();

            String url = "jdbc:sqlserver://" + ipadd + ";user=" + username + ";password=" + password;
            Connection con = DriverManager.getConnection(url);
            System.out.println("Connection established");
            System.out.println();

            System.out.print("Database name to insert generated data: ");
            String databaseName = in.nextLine();
            con.createStatement().execute("USE [" + databaseName + "]");
            System.out.println("Database found");

            System.out.println("Adding companies...");
            ArrayList<Company> companies = new ArrayList<>();
            for(int i = 0; i < 40; i++) {
                companies.add(Company.insertRandCompanyWithEmployees(con));
            }
            System.out.println("Finished");
            System.out.println();

            System.out.println("Adding individual participants...");
            ArrayList<Person> individuals = new ArrayList<>();
            for(int i = 0; i < 1000; i++) {
                individuals.add(Person.insertRandPerson(con));
            }
            System.out.println("Finished");
            System.out.println();

            LocalDate date = LocalDate.of(2013, 1, 1);

            System.out.println("Adding conferences...");
            for(int i = 0; i < 36; i++){
                int conferences = new Random().nextInt(4);
                for(int j = 0; j < conferences; j++) {
                    LocalDate randomDate = LocalDate.of(date.getYear(), date.getMonth(), 1 + new Random().nextInt(Month.from(date).maxLength()));
                    Conference.insertRandConference(con, randomDate, randomDate.plusDays(new Random().nextInt(3) + 1), companies, individuals);
                }
                date = date.plusMonths(1);
            }
            System.out.println("Finished");
            con.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
