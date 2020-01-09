package agh.db.generator;

import com.github.javafaker.Faker;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class Person {
    public String firstName;
    public String lastName;
    public String country;
    public String city;
    public String address;
    public String phone;
    public String email;

    public int personID;

    public Person(String firstName, String lastName, String country, String city, String address, String phone, String email){
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.email = email;

        for(Field f : Person.class.getFields()){
            try {
                if(f.get(this) instanceof String) {
                    f.set(this, ((String)f.get(this)).replace("'", "\'\'"));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Person randPerson() {
        Faker faker = new Faker();

        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String country = faker.address().country();
        String city = faker.address().city();
        String address = faker.address().streetAddress();
        String phone = faker.phoneNumber().cellPhone();
        String email = faker.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase());

        return new Person(
                firstName,
                lastName,
                country,
                city,
                address,
                phone,
                email
        );
    }

    public String insertSQL() {
        return
                        "INSERT INTO Participants (FirstName, LastName, Address, City, Country, Phone, Email)\n" +
                        "VALUES (" +
                        "'" + this.firstName + "', " +
                        "'" + this.lastName + "', " +
                        "'" + this.address + "', " +
                        "'" + this.city + "', " +
                        "'" + this.country + "', " +
                        "'" + this.phone + "', " +
                        "'" + this.email + "')";
    }

    public static Person insertRandPerson(Connection con) throws SQLException {
        Random rand = new Random();

        Person person = Person.randPerson();
        Statement st = con.createStatement();

        ResultSet rs = st.executeQuery(person.insertSQL() + "\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        person.personID = rs.getInt("LastPk");

        if(rand.nextInt(5) == 0){
            st.execute("INSERT INTO [Student Cards] (ParticipantID, CardID)\n" +
                    "VALUES (" + person.personID + ", " + (100000 + rand.nextInt(900000)) + ")"
            );
        }

        return person;
    }

    public void reserveConference(Connection con, Conference conf) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery("INSERT INTO [Conferences Participants Reservations] (ConferenceID, ParticipantID)\n" +
                "VALUES (" + conf.conferenceID + ", " + this.personID + ")\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        int reservationID = rs.getInt("LastPk");

        if(new Random().nextInt(30) != 0){
            con.createStatement().execute("INSERT INTO [Conference Payments] (ReservationID, Date)\n" +
                    "VALUES (" + reservationID + ", '" + conf.startDate.minusDays(14 + new Random().nextInt(20)).toString() + "')"
            );
        }
    }

    public void reserveConferenceDay(Connection con, ConferenceDay confDay) throws SQLException {
        con.createStatement().execute("INSERT INTO [Conference Day Participants] (ParticipantID, ConferenceDayID)\n" +
                "VALUES (" + this.personID + ", " + confDay.conferenceDayID + ")"
        );
    }

    public void reserveWorkshop(Connection con, Workshop ws) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery("INSERT INTO [Workshop Participants Reservations] (WorkshopID, ParticipantID)\n" +
                "VALUES (" + ws.workhopID + ", " + this.personID + ")\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        int reservationID = rs.getInt("LastPk");

        if(new Random().nextInt(30) != 0){
            con.createStatement().execute("INSERT INTO [Workshop Payments] (ReservationID, Date)\n" +
                    "VALUES (" + reservationID + ", '" + ws.conferenceDay.date.minusDays(3 + new Random().nextInt(20)).toString() + "')"
            );
        }
    }

    @Override
    public int hashCode() {
        return this.personID;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Person)
            return this.personID == ((Person) obj).personID;
        return false;
    }
}
