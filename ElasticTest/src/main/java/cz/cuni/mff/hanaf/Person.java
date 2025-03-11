package cz.cuni.mff.hanaf;

import java.util.Date;

public class Person {
    private int age;
    private String fullName;
    private Date dateOfBirth;

    public Person() {
    }

    // Constructor
    public Person(int age, String fullName, Date dateOfBirth) {
        this.age = age;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters
    public int getAge() {
        return age;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}
