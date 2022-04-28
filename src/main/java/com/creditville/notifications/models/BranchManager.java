package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BranchManager {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @Column(unique = true, nullable = false)
    private String branch;
    private String officerName;
    private String officerEmail;
    private String officerPhoneNo;
    private Date createdOn;
    private String accountNumber;
    private String accountName;
    private String bankName;


    public BranchManager(String branch, String officerName, String officerEmail, String officerPhoneNo,String accountNumber,String accountName, String bankName) {
        this.branch = branch;
        this.officerName = officerName;
        this.officerEmail = officerEmail;
        this.officerPhoneNo = officerPhoneNo;
        this.createdOn = new Date();
        this.accountName=accountName;
        this.accountNumber=accountNumber;
        this.bankName=bankName;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof BranchManager && (object == this || Objects.equals(this.getId(), ((BranchManager) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBranch(), getOfficerName(), getOfficerPhoneNo(), getCreatedOn());
    }
}
