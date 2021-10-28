package com.creditville.notifications.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"operationName", "eventDate"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
public class MailMonitor {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String operationName;
    private LocalDate eventDate;
    private Long successCount;
    private Long failedCount;
    @Version
    private Date lastUpdate;

    public MailMonitor(String operationName, LocalDate eventDate) {
        this.operationName = operationName;
        this.eventDate = eventDate;
        this.successCount = 1L;
        this.failedCount = 1L;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof MailMonitor && (object == this || Objects.equals(this.getId(), ((MailMonitor) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperationName(), getEventDate(), getSuccessCount(), getFailedCount());
    }
}
