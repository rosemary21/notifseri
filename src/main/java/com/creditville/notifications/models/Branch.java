package com.creditville.notifications.models;

/* Created by David on 07/06/2021 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;
    private Boolean isEnabled;

    public Branch(String name) {
        this.name = name;
        this.isEnabled = true;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof Branch && (object == this || Objects.equals(this.getId(), ((Branch) object).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
