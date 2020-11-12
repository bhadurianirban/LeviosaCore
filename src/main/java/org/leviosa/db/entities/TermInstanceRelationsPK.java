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
public class TermInstanceRelationsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "term_slug")
    private String termSlug;
    @Basic(optional = false)
    @Column(name = "term_meta_key")
    private String termMetaKey;
    @Basic(optional = false)
    @Column(name = "term_instance_slug")
    private String termInstanceSlug;
    @Basic(optional = false)
    @Column(name = "term_instance_rel_slug")
    private String termInstanceRelSlug;

    public TermInstanceRelationsPK() {
    }

    public TermInstanceRelationsPK(String termSlug, String termMetaKey, String termInstanceSlug, String termInstanceRelSlug) {
        this.termSlug = termSlug;
        this.termMetaKey = termMetaKey;
        this.termInstanceSlug = termInstanceSlug;
        this.termInstanceRelSlug = termInstanceRelSlug;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    public String getTermMetaKey() {
        return termMetaKey;
    }

    public void setTermMetaKey(String termMetaKey) {
        this.termMetaKey = termMetaKey;
    }

    public String getTermInstanceSlug() {
        return termInstanceSlug;
    }

    public void setTermInstanceSlug(String termInstanceSlug) {
        this.termInstanceSlug = termInstanceSlug;
    }

    public String getTermInstanceRelSlug() {
        return termInstanceRelSlug;
    }

    public void setTermInstanceRelSlug(String termInstanceRelSlug) {
        this.termInstanceRelSlug = termInstanceRelSlug;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termSlug != null ? termSlug.hashCode() : 0);
        hash += (termMetaKey != null ? termMetaKey.hashCode() : 0);
        hash += (termInstanceSlug != null ? termInstanceSlug.hashCode() : 0);
        hash += (termInstanceRelSlug != null ? termInstanceRelSlug.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermInstanceRelationsPK)) {
            return false;
        }
        TermInstanceRelationsPK other = (TermInstanceRelationsPK) object;
        if ((this.termSlug == null && other.termSlug != null) || (this.termSlug != null && !this.termSlug.equals(other.termSlug))) {
            return false;
        }
        if ((this.termMetaKey == null && other.termMetaKey != null) || (this.termMetaKey != null && !this.termMetaKey.equals(other.termMetaKey))) {
            return false;
        }
        if ((this.termInstanceSlug == null && other.termInstanceSlug != null) || (this.termInstanceSlug != null && !this.termInstanceSlug.equals(other.termInstanceSlug))) {
            return false;
        }
        if ((this.termInstanceRelSlug == null && other.termInstanceRelSlug != null) || (this.termInstanceRelSlug != null && !this.termInstanceRelSlug.equals(other.termInstanceRelSlug))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermInstanceRelationsPK[ termSlug=" + termSlug + ", termMetaKey=" + termMetaKey + ", termInstanceSlug=" + termInstanceSlug + ", termInstanceRelSlug=" + termInstanceRelSlug + " ]";
    }
    
}
