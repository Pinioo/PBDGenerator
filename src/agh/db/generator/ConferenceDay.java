package agh.db.generator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class ConferenceDay {
    public int places;
    public LocalDate date;

    public int conferenceDayID;
    public Conference conference;

    public ArrayList<Person> participants = new ArrayList<>();

    public HashMap<Integer, Integer> reservedPlaces = new HashMap<>();

    public ConferenceDay(Conference conference, int places, LocalDate date) {
        this.conference = conference;
        this.places = places;
        this.date = date;
    }

    public static ConferenceDay randConferenceDay(Conference conference, LocalDate date){
        Random rand = new Random();

        ConferenceDay toReturn = new ConferenceDay(conference, new Random().nextInt(60) + 170, date);

        return toReturn;
    }

    public String conferenceDaySQL(){
        return "INSERT INTO [Conferences Days] (ConferenceID, Day, Places) VALUES\n" +
                "(" + this.conference.conferenceID + ", '" + this.date.toString() + "', " + this.places + ")";
    }

    public static ConferenceDay insertRandConferenceDay(Connection con, Conference conference, LocalDate date) throws SQLException {
        Random rand = new Random();

        ConferenceDay confDay = ConferenceDay.randConferenceDay(conference, date);

        ResultSet rs = con.createStatement().executeQuery(confDay.conferenceDaySQL() + "\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        confDay.conferenceDayID = rs.getInt("LastPk");


        int placesLeft = confDay.places - new Random().nextInt(10);

        HashSet<Integer> addedPeople = new HashSet<>();
        int participantsCount = conference.participants.size();
        int i = 0;

        Person chosenPerson;

        //TODO: Reserve days by companies
        while(placesLeft > 0 && i < 500){
            chosenPerson = conference.participants.get(rand.nextInt(participantsCount));
            if(!addedPeople.contains(chosenPerson.personID)){
                addedPeople.add(chosenPerson.personID);
                confDay.participants.add(chosenPerson);
                chosenPerson.reserveConferenceDay(con, confDay);
                placesLeft--;
            }
            i++;
        }

        int workshops = rand.nextInt(5);

        for(i = 0; i < workshops; i++){
            Workshop.insertRandWorkshop(con, confDay);
        }

        return confDay;
    }
}
