package agh.db.generator;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Conference {
    public String name;
    public double studentDiscount;
    public LocalDate startDate;

    public int conferenceID;

    public HashMap<Company, ArrayList<Person>> companiesParticipants = new HashMap<>();
    public ArrayList<Person> participants = new ArrayList<>();

    public Conference(String name, double studentDiscount){
        this.name = name;
        this.studentDiscount = studentDiscount;
    }

    public final static String[] firstNamePart = {
            "C#", "Python", "C", "Java", "SQL", "Erlang", "Haskell", "Assembler",
            "F#", "Pascal", "Icon", "Ada", "Scala", "Q#", "BASIC", "R", "JavaScript",
            "PHP", "Angular", "Ruby", "Kotlin"
    };

    public final static String[] secondNamePart = {
            "techniques", "frameworks", "fundamentals", "elements", "development",
            "teaching", "optimization", "usages", "improvements", "libraries",
            "compilers", ""
    };

    public final static String[] thirdNamePart = {
            "from scratch", "for developers", "in a nutshell", "for students",
            "in accounting", "in business", "for advanced", "for beginners",
            "knowledge", "then and now", "future", ""
    };

    public static Conference randConference(){
        Random rand = new Random();
        return new Conference(
                firstNamePart[rand.nextInt(firstNamePart.length)] + " " +
                        secondNamePart[rand.nextInt(secondNamePart.length)] + " " +
                        thirdNamePart[rand.nextInt(thirdNamePart.length)],
                rand.nextDouble()/5 + 0.1);
    }

    // TODO: make it non-static
    public static void insertRandomPriceLevels(Connection con, int conferenceID, LocalDate startDate) throws SQLException {
        Statement st = con.createStatement();

        Random rand = new Random();
        int levels = 2 + rand.nextInt(2);

        LocalDate priceLevelDate = startDate.minusDays(14);
        int priceLevel = 300;

        for(int i = 0; i < levels; i++){
            priceLevelDate = priceLevelDate.minusDays(5 + rand.nextInt(4));
            st.execute("USE [Conference Organizations]\n" +
                    "INSERT INTO [Conferences Price Levels] (ConferenceID, PriceLevel, DateFrom)\n" +
                    "VALUES (" + conferenceID + ", " + (priceLevel - 20 - rand.nextInt(50)) + ", '" + priceLevelDate.toString() + "')");
        }
    }

    // TODO: make it non-static
    public static Conference insertRandConference(Connection con, LocalDate startDate, LocalDate endDate, ArrayList<Company> companies, ArrayList<Person> individuals) throws SQLException {
        Random rand = new Random();

        Conference conference = Conference.randConference();
        conference.startDate = startDate;

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(conference.conferenceSQL() + "\n" +
            "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        conference.conferenceID = rs.getInt("LastPk");

        insertRandomPriceLevels(con, conference.conferenceID, conference.startDate);

        for(Company company: companies){
            if(rand.nextInt(3) == 0){
                conference.companiesParticipants.put(company, new ArrayList<>());
                company.reserveConference(con, conference, startDate.minusDays(34 - rand.nextInt(20)));
                for(Person emp : company.employees){
                    if(rand.nextInt(2) == 0){
                        emp.reserveConference(con, conference);
                        conference.companiesParticipants.get(company).add(emp);
                    }
                }
            }
        }

        for(Person person: individuals){
            if(rand.nextInt(4) == 0){
                person.reserveConference(con, conference);
                conference.participants.add(person);
            }
        }

        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            try {
                ConferenceDay.insertRandConferenceDay(con, conference, date);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return conference;
    }

    private String conferenceSQL() {
        return "INSERT INTO Conferences (Name, StudentDiscount)\n" +
                        "VALUES (" +
                        "'" +
                        this.name +
                        "', " +
                        this.studentDiscount + ")";
    }
}
