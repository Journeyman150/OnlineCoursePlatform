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
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Person VALUES(?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, person.getId());
            preparedStatement.setString(2, person.getName());
            preparedStatement.setString(3, person.getSurname());
            preparedStatement.setInt(4, person.getAge());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.executeUpdate();
            peopleList.add(person);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Person getPersonById(int id) {
        Person person = null;
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * FROM Person WHERE id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            person = new Person();
            resultSet.next();
            person.setId(resultSet.getInt("id"));
            person.setName(resultSet.getString("name"));
            person.setSurname(resultSet.getString("surname"));
            person.setAge(resultSet.getInt("age"));
            person.setEmail(resultSet.getString("email"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    public void update(int id, Person updatedPerson) {
        Person personToBeUpdated = this.getPersonById(id);
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("UPDATE Person SET " +
                            "name = ?, " +
                            "surname = ?, " +
                            "age = ?, " +
                            "email = ? " +
                            "WHERE id = ?");
            preparedStatement.setString(1, updatedPerson.getName());
            preparedStatement.setString(2, updatedPerson.getSurname());
            preparedStatement.setInt(3, updatedPerson.getAge());
            preparedStatement.setString(4, updatedPerson.getEmail());
            preparedStatement.setInt(5, updatedPerson.getId());
            preparedStatement.executeUpdate();
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
            PreparedStatement preparedStatement =
                    connection.prepareStatement("DELETE FROM Person WHERE id = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            peopleList.removeIf(p -> p.getId() == id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
