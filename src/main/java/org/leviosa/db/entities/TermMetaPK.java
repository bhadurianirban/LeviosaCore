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
public class TermMetaPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "term_slug")
    private String termSlug;
    @Basic(optional = false)
    @Column(name = "meta_key")
    private String metaKey;

    public TermMetaPK() {
    }

    public TermMetaPK(String termSlug, String metaKey) {
        this.termSlug = termSlug;
        this.metaKey = metaKey;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    public String getMetaKey() {
        return metaKey;
    }

    public void setMetaKey(String metaKey) {
        this.metaKey = metaKey;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termSlug != null ? termSlug.hashCode() : 0);
        hash += (metaKey != null ? metaKey.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermMetaPK)) {
            return false;
        }
        TermMetaPK other = (TermMetaPK) object;
        if ((this.termSlug == null && other.termSlug != null) || (this.termSlug != null && !this.termSlug.equals(other.termSlug))) {
            return false;
        }
        if ((this.metaKey == null && other.metaKey != null) || (this.metaKey != null && !this.metaKey.equals(other.metaKey))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermMetaPK[ termSlug=" + termSlug + ", metaKey=" + metaKey + " ]";
    }
    
}
