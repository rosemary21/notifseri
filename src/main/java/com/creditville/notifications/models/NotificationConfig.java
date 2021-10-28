package com.creditville.notifications.models;

/* Created by David on 6/10/2021 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"notificationType", "branch_id"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
public class NotificationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    private NotificationType notificationType;
    @ManyToOne
    private Branch branch;
    private Boolean isEnabled;

    public NotificationConfig(NotificationType notificationType, Branch branch) {
        this.notificationType = notificationType;
        this.branch = branch;
        this.isEnabled = true;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof NotificationConfig && (object == this || Objects.equals(this.getId(), ((NotificationConfig) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNotificationType(), getBranch().getId());
    }
}
