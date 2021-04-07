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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"toAddress", "toName", "subject", "paymentDate", "customIdentifier"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmailAudit {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String toAddress;
    private String toName;
    private String subject;
    private Date sentOn;
    private LocalDate paymentDate;
    private String customIdentifier;

    public EmailAudit(String toAddress, String toName, String subject, LocalDate paymentDate, String customIdentifier) {
        this.toAddress = toAddress;
        this.toName = toName;
        this.subject = subject;
        this.sentOn = new Date();
        this.paymentDate = paymentDate;
        this.customIdentifier = customIdentifier;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof EmailAudit && (object == this || Objects.equals(this.getId(), ((EmailAudit) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getSubject(), getPaymentDate(), getCustomIdentifier());
    }
}
