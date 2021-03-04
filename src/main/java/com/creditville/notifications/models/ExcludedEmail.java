package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Chuks on 02/09/2021.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExcludedEmail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(unique = true, nullable = false)
    private String emailAddress;
    private Date createdOn;

    public ExcludedEmail(String emailAddress) {
        this.emailAddress = emailAddress;
        this.createdOn = new Date();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof ExcludedEmail && (object == this || Objects.equals(this.getId(), ((ExcludedEmail) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmailAddress());
    }
}
