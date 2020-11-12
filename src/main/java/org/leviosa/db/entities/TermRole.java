/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.entities;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bhaduri
 */
@Entity
@Table(name = "term_role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TermRole.findAll", query = "SELECT t FROM TermRole t")})
public class TermRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TermRolePK termRolePK;
    @JoinColumn(name = "term_slug", referencedColumnName = "term_slug", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Term term;

    public TermRole() {
    }

    public TermRole(TermRolePK termRolePK) {
        this.termRolePK = termRolePK;
    }

    public TermRole(int roleId, String termSlug) {
        this.termRolePK = new TermRolePK(roleId, termSlug);
    }

    public TermRolePK getTermRolePK() {
        return termRolePK;
    }

    public void setTermRolePK(TermRolePK termRolePK) {
        this.termRolePK = termRolePK;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termRolePK != null ? termRolePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermRole)) {
            return false;
        }
        TermRole other = (TermRole) object;
        if ((this.termRolePK == null && other.termRolePK != null) || (this.termRolePK != null && !this.termRolePK.equals(other.termRolePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermRole[ termRolePK=" + termRolePK + " ]";
    }
    
}
