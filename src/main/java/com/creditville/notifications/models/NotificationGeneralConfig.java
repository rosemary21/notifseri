package com.creditville.notifications.models;

/* Created by David on 6/10/2021 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NotificationGeneralConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    @Enumerated(value = EnumType.STRING)
    private NotificationType notificationType;
    private Boolean isEnabled;

    public NotificationGeneralConfig(NotificationType notificationType) {
        this.notificationType = notificationType;
        this.isEnabled = true;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof NotificationGeneralConfig && (object == this || Objects.equals(this.getId(), ((NotificationGeneralConfig) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNotificationType());
    }
}
