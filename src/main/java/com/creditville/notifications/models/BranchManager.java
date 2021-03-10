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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(unique = true, nullable = false)
    private String branch;
    private String officerName;
    private String officerEmail;
    private String officerPhoneNo;
    private Date createdOn;

    public BranchManager(String branch, String officerName, String officerEmail, String officerPhoneNo) {
        this.branch = branch;
        this.officerName = officerName;
        this.officerEmail = officerEmail;
        this.officerPhoneNo = officerPhoneNo;
        this.createdOn = new Date();
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
