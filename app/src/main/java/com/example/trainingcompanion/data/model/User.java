package com.example.trainingcompanion.data.model;

public class User {
    private int age;
    private int weight;
    private int height;
    private String sex;

    public User(int age, int weight, int height, String sex) {
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "User{" +
                "age=" + age +
                ", weight=" + weight +
                ", height=" + height +
                ", sex='" + sex + '\'' +
                '}';
    }
}
