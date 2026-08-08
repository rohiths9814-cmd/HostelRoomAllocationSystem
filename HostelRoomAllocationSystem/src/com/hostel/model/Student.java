package com.hostel.model;

public class Student {

    private String registerNumber;
    private String name;
    private String department;
    private int year;
    private String phone;

    public Student() {
        System.out.println("Student constructor called");
    }

    public Student(String registerNumber, String name, String department, int year, String phone) {

            setRegisterNumber(registerNumber);
            setName(name);
            setDepartment(department);
            setYear(year);
            setPhone(phone);
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        
        if (year >= 1 && year <= 4) {
            this.year = year;
        } else {
            System.out.println("Invalid year.");
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        
        if(phone != null && phone.length() == 10) {
            this.phone = phone;
        } else {
            System.out.println("Invalid phone number.");
        }
    }
}