package org.icatproject.authn_db;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="PASSWD")
public class Passwd implements Serializable {

    @Id
    @Column(name="USERNAME")
    private String userName;

    @Column(name="ENCODEDPASSWORD")
    private String encodedPassword;

    // Getter for encodedPassword
    public String getEncodedPassword() {
        return encodedPassword;
    }
}
