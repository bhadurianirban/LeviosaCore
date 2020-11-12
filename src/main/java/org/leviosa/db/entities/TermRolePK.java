/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author bhaduri
 */
@Embeddable
public class TermRolePK implements Serializable {

    @Basic(optional = false)
    @Column(name = "role_id")
    private int roleId;
    @Basic(optional = false)
    @Column(name = "term_slug")
    private String termSlug;

    public TermRolePK() {
    }

    public TermRolePK(int roleId, String termSlug) {
        this.roleId = roleId;
        this.termSlug = termSlug;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) roleId;
        hash += (termSlug != null ? termSlug.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermRolePK)) {
            return false;
        }
        TermRolePK other = (TermRolePK) object;
        if (this.roleId != other.roleId) {
            return false;
        }
        if ((this.termSlug == null && other.termSlug != null) || (this.termSlug != null && !this.termSlug.equals(other.termSlug))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermRolePK[ roleId=" + roleId + ", termSlug=" + termSlug + " ]";
    }
    
}
