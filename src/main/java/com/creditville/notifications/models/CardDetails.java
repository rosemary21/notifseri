package com.creditville.notifications.models;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Entity
@Data
public class CardDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;
    private BigDecimal amount;
    private String firstName;
    private String lastName;
    private String email;
    private String accountName;
    private String reference;
    private String channel;
    private String status;
    private BigDecimal cardTokenCharge;
    private String authorizationCode;
    private String signature;
    @Lob
    private String paystackResponse;
//    @Version
//    private Date lastUpdate;

//    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private Collection<CardTransactions> transactions;

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof CardDetails && (object == this || Objects.equals(this.getId(), ((CardDetails) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClientId(), getEmail(), getStatus(), getAuthorizationCode());
    }
}
