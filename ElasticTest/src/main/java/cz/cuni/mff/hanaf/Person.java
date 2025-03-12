package cz.cuni.mff.hanaf;

public class Person {
    private int age;
    private String fullName;

    public Person() {
    }

    public Person(int age, String fullName) {
        this.age = age;
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public String getFullName() {
        return fullName;
    }
}
