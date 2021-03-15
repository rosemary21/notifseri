package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PartialDebitAttempt {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private LocalDate date;
    private Integer totalNoOfAttempts;
    @ManyToOne
    private PartialDebit partialDebit;

    public PartialDebitAttempt(PartialDebit partialDebit) {
        this.date = LocalDate.now();
        this.totalNoOfAttempts = 1;
        this.partialDebit = partialDebit;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof PartialDebitAttempt && (object == this || Objects.equals(this.getId(), ((PartialDebitAttempt) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPartialDebit().getId(), getDate());
    }
}
