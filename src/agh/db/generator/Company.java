package agh.db.generator;

import com.github.javafaker.Faker;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Company {
    public int companyID;

    public String name;
    public String country;
    public String city;
    public String address;
    public String phone;
    public String email;

    public ArrayList<Person> employees = new ArrayList<>();

    public Company(String name, String country, String city, String address, String phone, String email){
        this.name = name;
        this.country = country;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.email = email;

        for(Field f : Company.class.getFields()){
            try {
                if(f.get(this) instanceof String) {
                    f.set(this, ((String)f.get(this)).replace("'", "\'\'"));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Company randCompany(){
        Faker faker = new Faker();

        String name = faker.company().name();
        String country = faker.address().country();
        String city = faker.address().city();
        String address = faker.address().streetAddress();
        String phone = faker.phoneNumber().cellPhone();
        String email = faker.internet().emailAddress();

        return new Company(
                name,
                country,
                city,
                address,
                phone,
                email
        );
    }

    public void reserveConference(Connection con, Conference conf, LocalDate date) throws SQLException {
        con.createStatement().execute(
                "INSERT INTO [Companies Conferences] (ConferenceID, CompanyID, Date)\n" +
                "VALUES (" + conf.conferenceID + ", " + this.companyID + ", '" + date.toString() + "')"
        );
    }

    public void reserveConferenceDay(Connection con, ConferenceDay confDay, int places) throws SQLException {
        con.createStatement().execute(
                "INSERT INTO [Companies Conference Days] (ConferenceDayID, CompanyID, ReservedPlaces)\n" +
                        "VALUES (" + confDay.conferenceDayID + ", " + this.companyID + ", " + places + ")"
        );
    }

    public String companySQL() {
        return "INSERT INTO Companies (Name, Address, City, Country, Phone, Email)\n" +
                        "VALUES (" +
                        "'" + this.name + "', " +
                        "'" + this.address + "', " +
                        "'" + this.city + "', " +
                        "'" + this.country + "', " +
                        "'" + this.phone + "', " +
                        "'" + this.email + "')";
    }

    public void addEmployee(Connection con, Person emp) throws SQLException {
        this.employees.add(emp);
        con.createStatement().execute("INSERT INTO [Participant Companies] (ParticipantID, CompanyID)\n" +
                "VALUES (" + emp.personID + ", " + this.companyID + ")"
        );
    }

    public static Company insertRandCompanyWithEmployees(Connection con) throws SQLException {
        Company company = Company.randCompany();
        Statement st = con.createStatement();

        ResultSet rs = st.executeQuery(company.companySQL() + "\n" +
                "SELECT SCOPE_IDENTITY() AS LastPk"
        );

        rs.next();
        company.companyID = rs.getInt("LastPk");

        int employeesCount = 50 + new Random().nextInt(30);

        for(int i = 0; i < employeesCount; i++){
            Person emp = Person.insertRandPerson(con);
            company.addEmployee(con, emp);
        }

        return company;
    }

    @Override
    public int hashCode() {
        return this.companyID;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Company){
            return this.companyID == ((Company) obj).companyID;
        }
        return false;
    }
}
