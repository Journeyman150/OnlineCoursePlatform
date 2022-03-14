package com.example.people;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("singleton")
@PropertySource("classpath:application.properties")
public class PeopleDAO {
    private List<Person> peopleList;

    private static final String URL = "jdbc:postgresql://localhost:5432/people_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    private static Connection connection;
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    public PeopleDAO() {
        peopleList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String SQL = "SELECT * FROM Person";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                Person person = new Person();
                person.setId(resultSet.getInt("id"));
                person.setName(resultSet.getString("name"));
                person.setSurname(resultSet.getString("surname"));
                person.setAge(resultSet.getInt("age"));
                person.setEmail(resultSet.getString("email"));
                peopleList.add(person);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Person> getPeopleList() {
        return peopleList;
    }

    public void setPeopleList(List<Person> peopleList) {
        this.peopleList = peopleList;
    }

    public void addPerson(Person person) {
        if (peopleList.size() != 0)
            person.setId(peopleList.get(peopleList.size()-1).getId() + 1);
        try {
            Statement statement = connection.createStatement();
            String SQL = "INSERT INTO Person VALUES ('" + person.getId() + "', '" + person.getName() + "', '" + person.getSurname()
                    + "', '" + person.getAge() + "', '" + person.getEmail() + "')";
            statement.execute(SQL);
            peopleList.add(person);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Person getPersonById(int id) {
        return peopleList.stream().filter(p -> p.getId() == id).findAny().get();
    }

    public void update(int id, Person updatedPerson) {
        Person personToBeUpdated = this.getPersonById(id);
        try {
            Statement statement = connection.createStatement();
            String SQL = "UPDATE Person SET " +
                    "name =" + " '" + updatedPerson.getName() + "', " +
                    "surname =" + " '" + updatedPerson.getSurname() + "', " +
                    "age =" + " " + updatedPerson.getAge() + ", " +
                    "email =" + " '" + updatedPerson.getEmail() + "'" +
                    "WHERE id = " + personToBeUpdated.getId();
            statement.execute(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        personToBeUpdated.setName(updatedPerson.getName());
        personToBeUpdated.setSurname(updatedPerson.getSurname());
        personToBeUpdated.setAge(updatedPerson.getAge());
        personToBeUpdated.setEmail(updatedPerson.getEmail());
    }
    public void deletePerson(int id) {
        try {
            Statement statement = connection.createStatement();
            String SQL = "DELETE FROM Person WHERE id = " + id;
            statement.execute(SQL);
            peopleList.removeIf(p -> p.getId() == id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
