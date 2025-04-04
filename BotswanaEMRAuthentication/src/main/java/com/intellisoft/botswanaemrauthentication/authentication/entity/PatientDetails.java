package com.intellisoft.botswanaemrauthentication.authentication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class PatientDetails {

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000;   // 5 minutes

    @Id
    private String id;

    @Column(unique = true)
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String patientIdentificationNo;
    private String nationalPassportNo;
    private String identificationType;
    private String username;
    private String openMrsId;

    private boolean isPatient;

    private boolean verified;

    @JsonIgnore
    private String oneTimeLink;
    @JsonIgnore
    private Date otpRequestedTime;
    @JsonIgnore
    private String userKeycloakId;

    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;

    @JsonIgnore
    private String profileUrl;

    public boolean isOTPValid() {
        if (this.getOneTimeLink() == null) {
            return false;
        }

        long currentTimeInMillis = System.currentTimeMillis();
        long otpRequestedTimeInMillis = this.otpRequestedTime.getTime();

        long otpExpiryTime = otpRequestedTimeInMillis + OTP_VALID_DURATION;

        // OTP expires
        return otpRequestedTimeInMillis + OTP_VALID_DURATION >= currentTimeInMillis;
    }

    public PatientDetails() {
    }

    public PatientDetails(String firstName, String lastName, String phoneNumber, String dateOfBirth,
                          String gender, String patientIdentificationNo, String nationalPassportNo,
                          String identificationType, String emailAddress, String userKeycloakId,
                          String username, boolean isPatient, String profileUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.patientIdentificationNo = patientIdentificationNo;
        this.nationalPassportNo = nationalPassportNo;
        this.identificationType = identificationType;
        this.emailAddress = emailAddress;
        this.userKeycloakId = userKeycloakId;
        this.username = username;
        this.isPatient = isPatient;
        this.profileUrl = profileUrl;

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPatientIdentificationNo() {
        return patientIdentificationNo;
    }

    public void setPatientIdentificationNo(String patientIdentificationNo) {
        this.patientIdentificationNo = patientIdentificationNo;
    }

    public String getNationalPassportNo() {
        return nationalPassportNo;
    }

    public void setNationalPassportNo(String nationalPassportNo) {
        this.nationalPassportNo = nationalPassportNo;
    }

    public String getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(String identificationType) {
        this.identificationType = identificationType;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getOneTimeLink() {
        return oneTimeLink;
    }

    public void setOneTimeLink(String oneTimeLink) {
        this.oneTimeLink = oneTimeLink;
    }

    public Date getOtpRequestedTime() {
        return otpRequestedTime;
    }

    public void setOtpRequestedTime(Date otpRequestedTime) {
        this.otpRequestedTime = otpRequestedTime;
    }

    public String getUserKeycloakId() {
        return userKeycloakId;
    }

    public void setUserKeycloakId(String userKeycloakId) {
        this.userKeycloakId = userKeycloakId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isPatient() {
        return isPatient;
    }

    public void setPatient(boolean patient) {
        isPatient = patient;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getOpenMrsId() {
        return openMrsId;
    }

    public void setOpenMrsId(String openMrsId) {
        this.openMrsId = openMrsId;
    }
}
