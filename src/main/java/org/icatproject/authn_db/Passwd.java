package org.icatproject.authn_db;

import jakarta.persistence.*;
import org.icatproject.authentication.AuthnException;
import org.icatproject.authentication.PasswordChecker;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.net.HttpURLConnection;

@Entity
@Table(name="PASSWD")
public class Passwd implements Serializable {

    private static final Logger logger = Logger.getLogger(Passwd.class);

    @Id
    @Column(name="USERNAME")
    private String userName;

    @Column(name="ENCODEDPASSWORD")
    private String password;

    // Getter for encodedPassword
    public String getPassword() {
        return this.password;
    }

    /** Check the password in the request matches the one in the database */
    public void checkPassword(String requestPassword, String dbPassword) throws AuthnException {
        logger.debug("Checking password against database");
        if (!PasswordChecker.verify(requestPassword, dbPassword)) {
            throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "The username and password do not match");
        }
    }
}
