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
import javax.persistence.JoinColumns;
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
@Table(name = "term_instance_relations")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TermInstanceRelations.findAll", query = "SELECT t FROM TermInstanceRelations t")
    
})
public class TermInstanceRelations implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TermInstanceRelationsPK termInstanceRelationsPK;
    @JoinColumns({
        @JoinColumn(name = "term_slug", referencedColumnName = "term_slug", insertable = false, updatable = false)
        , @JoinColumn(name = "term_meta_key", referencedColumnName = "meta_key", insertable = false, updatable = false)
        , @JoinColumn(name = "term_instance_slug", referencedColumnName = "term_instance_slug", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private TermInstance termInstance;

    public TermInstanceRelations() {
    }

    public TermInstanceRelations(TermInstanceRelationsPK termInstanceRelationsPK) {
        this.termInstanceRelationsPK = termInstanceRelationsPK;
    }

    public TermInstanceRelations(String termSlug, String termMetaKey, String termInstanceSlug, String termInstanceRelSlug) {
        this.termInstanceRelationsPK = new TermInstanceRelationsPK(termSlug, termMetaKey, termInstanceSlug, termInstanceRelSlug);
    }

    public TermInstanceRelationsPK getTermInstanceRelationsPK() {
        return termInstanceRelationsPK;
    }

    public void setTermInstanceRelationsPK(TermInstanceRelationsPK termInstanceRelationsPK) {
        this.termInstanceRelationsPK = termInstanceRelationsPK;
    }

    public TermInstance getTermInstance() {
        return termInstance;
    }

    public void setTermInstance(TermInstance termInstance) {
        this.termInstance = termInstance;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termInstanceRelationsPK != null ? termInstanceRelationsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermInstanceRelations)) {
            return false;
        }
        TermInstanceRelations other = (TermInstanceRelations) object;
        if ((this.termInstanceRelationsPK == null && other.termInstanceRelationsPK != null) || (this.termInstanceRelationsPK != null && !this.termInstanceRelationsPK.equals(other.termInstanceRelationsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermInstanceRelations[ termInstanceRelationsPK=" + termInstanceRelationsPK + " ]";
    }
    
}
