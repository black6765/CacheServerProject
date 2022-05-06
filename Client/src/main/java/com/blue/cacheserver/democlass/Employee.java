package com.blue.cacheserver.democlass;

import java.io.Serializable;

public class Employee implements Serializable {
    private String name;
    private int age;
    private String department;
    private String phoneNumber;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", department='" + department + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    public String getDepartment() {
        return department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Employee(String name, int age, String department, String phoneNumber) {
        this.name = name;
        this.age = age;
        this.department = department;
        this.phoneNumber = phoneNumber;
    }

}
