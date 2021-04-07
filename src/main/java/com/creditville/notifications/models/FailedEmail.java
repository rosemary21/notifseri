package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Chuks on 02/09/2021.
 */
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"toAddress", "subject", "paymentDate", "customIdentifier"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
public class FailedEmail {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String toName;
    private String toAddress;
    private String subject;
    @Lob
    private String message;
    private Integer noOfAttempts;
    private Date createdOn;
    private LocalDate paymentDate;
    @Lob
    private String reason;
    @Version
    private Date lastUpdatedOn;
    private String customIdentifier;

    public FailedEmail(String toName, String toAddress, String subject, String message, LocalDate paymentDate, String reason, String customIdentifier) {
        this.toName = toName;
        this.toAddress = toAddress;
        this.subject = subject;
        this.message = message;
        this.noOfAttempts = 1;
        this.createdOn = new Date();
        this.paymentDate = paymentDate;
        this.reason = reason;
        this.customIdentifier = customIdentifier;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof FailedEmail && (object == this || Objects.equals(this.getId(), ((FailedEmail) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getSubject(), getPaymentDate(), getCustomIdentifier());
    }
}
