package agh.db.generator;

import com.github.javafaker.Faker;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Random;

public class Workshop {
    public int workhopID;
    public String name;
    public int places;
    public int price;
    public ConferenceDay conferenceDay;
    public LocalTime startTime;
    public LocalTime endTime;

    public Workshop(String name, int places, int price, ConferenceDay conferenceDay, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.places = places;
        this.price = price;
        this.conferenceDay = conferenceDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String workshopSQL(){
        return "INSERT INTO Workshops (Name, Places, Price, ConferenceDayID, StartTime, EndTime)\n" +
                "VALUES ('" + this.name + "', " + this.places + ", " + this.price + ", " + this.conferenceDay.conferenceDayID + ", '" + this.startTime.toString() + "', '" + this.endTime.toString()+ "')";
    }

    public static Workshop randWorkhop(ConferenceDay conferenceDay){
        Random rand = new Random();
        LocalTime startTime = LocalTime.of(10 + rand.nextInt(6), 0);

        return new Workshop(
                new Faker().book().title().replace("'", "''"),
                40 + rand.nextInt(20),
                rand.nextInt(5) == 0 ? 0 : 20 + rand.nextInt(20),
                conferenceDay,
                startTime,
                startTime.plusHours(2 + rand.nextInt(3))
        );
    }

    public static Workshop insertRandWorkshop(Connection con, ConferenceDay confDay) throws SQLException {
        Random rand = new Random();

        Workshop workshop = Workshop.randWorkhop(confDay);

        ResultSet rs = con.createStatement().executeQuery(workshop.workshopSQL() + "\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        workshop.workhopID = rs.getInt("LastPk");

        int placesLeft = workshop.places - rand.nextInt(5);

        HashSet<Integer> addedPeople = new HashSet<>();
        int participantsCount = confDay.participants.size();
        int i = 0;

        Person chosenPerson;
        while(placesLeft > 0 && i < 1000){
            chosenPerson = confDay.participants.get(rand.nextInt(participantsCount));
            if(!addedPeople.contains(chosenPerson.personID)){
                addedPeople.add(chosenPerson.personID);
                confDay.participants.add(chosenPerson);
                chosenPerson.reserveWorkshop(con, workshop);
                placesLeft--;
            }
            i++;
        }

        return workshop;
    }
}
