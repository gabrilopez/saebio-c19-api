package org.saebio.sample;

import java.time.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class Sample {
    private static int episodeLength;

    private LocalDate registryDate;
    private String patientName;
    private String patientSurname;
    private LocalDate birthDate;
    private String NHC;
    private String petition;
    private String service;
    private String criteria;
    private String resultPCR = null;
    private String resultTMA = null;
    private String sex = null;
    private Integer age = null;
    private String origin = null;
    private String reason = null;
    private String variant = null;
    private String lineage = null;
    private int episode;
    private String normalizedResult =  null;

    public static void setEpisodeLength(int episodeLength) {
        Sample.episodeLength = episodeLength;
    }

    public LocalDate getRegistryDate() {
        return registryDate;
    }

    public void setRegistryDate(LocalDate registryDate) {
        this.registryDate = registryDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNHC() {
        return NHC;
    }

    public void setNHC(String NHC) {
        this.NHC = NHC;
    }

    public String getPetition() {
        return petition;
    }

    public void setPetition(String petition) {
        this.petition = petition;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getResultPCR() {
        return resultPCR;
    }

    public void setResultPCR(String resultPCR) {
        this.resultPCR = resultPCR;
    }

    public String getResultTMA() {
        return resultTMA;
    }

    public void setResultTMA(String resultTMA) {
        this.resultTMA = resultTMA;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public String getNormalizedResult() {
        return normalizedResult;
    }

    public void setNormalizedResult(String normalizedResult) {
        this.normalizedResult = normalizedResult;
    }

    public boolean belongToSameEpisode(Sample oldSample) {
        long daysBetween = DAYS.between(oldSample.getRegistryDate(), this.getRegistryDate());
        return Math.abs(daysBetween) < episodeLength && this.getNHC().equals(oldSample.getNHC());
    }
}
