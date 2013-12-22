package com.ww.server.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author sandy
 */
@Entity
@Table(name = "accounts")
public class Account implements Serializable {

    private String accountId;
    private String accountName;
    private String accountEmail;
    private String accountPassword;
    private Date accountDateOfBirth;

    @Id
    @Column(name = "accountid")
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Column(name = "name")
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Column(name = "email")
    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    @Column(name = "hash")
    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dateofbirth")
    public Date getAccountDateOfBirth() {
        return accountDateOfBirth;
    }

    public void setAccountDateOfBirth(Date accountDateOfBirth) {
        this.accountDateOfBirth = accountDateOfBirth;
    }


}
