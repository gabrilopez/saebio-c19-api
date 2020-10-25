package org.saebio.sample;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Sample {
    private int petition;
    private LocalDate registryDate;
    private String hospital;
    private String hospitalService;
    private String destination;
    private String prescriptor;
    private String NHC;
    private String patient;
    private String sex;
    private String age;
    private LocalDate birthDate;
    private int month;
    private int year;
    private String type;
    private String result;

    public Sample() { }

    public Sample(int petition, LocalDate registryDate, String hospital, String hospitalService, String destination, String prescriptor, String NHC, String patient, String sex, String age, LocalDate birthDate, int month, int year, String type, String result) {
        this.petition = petition;
        this.registryDate = registryDate;
        this.hospital = hospital;
        this.hospitalService = hospitalService;
        this.destination = destination;
        this.prescriptor = prescriptor;
        this.NHC = NHC;
        this.patient = patient;
        this.sex = sex;
        this.age = age;
        this.birthDate = birthDate;
        this.month = month;
        this.year = year;
        this.type = type;
        this.result = result;
    }

    public int getPetition() {
        return petition;
    }

    public void setPetition(int petition) {
        this.petition = petition;
    }

    public LocalDate getRegistryDate() {
        return registryDate;
    }

    public void setRegistryDate(LocalDate registryDate) {
        this.registryDate = registryDate;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getHospitalService() {
        return hospitalService;
    }

    public void setHospitalService(String hospitalService) {
        this.hospitalService = hospitalService;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPrescriptor() {
        return prescriptor;
    }

    public void setPrescriptor(String prescriptor) {
        this.prescriptor = prescriptor;
    }

    public String getNHC() {
        return NHC;
    }

    public void setNHC(String NHC) {
        this.NHC = NHC;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
