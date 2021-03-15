package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PartialDebit {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Date createdOn;
    private LocalDate paymentDate;
    private String authorizationCode;
    private String loanId;
    private String currency = "NGN";
    private BigDecimal amount;
    private String email;
    private String atLeast = "2000";

    public PartialDebit(String authorizationCode, String loanId, BigDecimal amount, String email, LocalDate paymentDate) {
        this.createdOn = new Date();
        this.paymentDate = paymentDate;
        this.authorizationCode = authorizationCode;
        this.loanId = loanId;
        this.amount = amount;
        this.email = email;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof PartialDebit && (object == this || Objects.equals(this.getId(), ((PartialDebit) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLoanId(), getAuthorizationCode(), getAmount(), getEmail());
    }
}
