/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author bhaduri
 */
@Entity
@Table(name = "term_instance")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TermInstance.findAll", query = "SELECT t FROM TermInstance t")
})
public class TermInstance implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TermInstancePK termInstancePK;
    @Column(name = "term_instance_value")
    private String termInstanceValue;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "termInstance")
    private List<TermInstanceRelations> termInstanceRelationsList;
    @JoinColumns({
        @JoinColumn(name = "term_slug", referencedColumnName = "term_slug", insertable = false, updatable = false)
        , @JoinColumn(name = "meta_key", referencedColumnName = "meta_key", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private TermMeta termMeta;

    public TermInstance() {
    }

    public TermInstance(TermInstancePK termInstancePK) {
        this.termInstancePK = termInstancePK;
    }

    public TermInstance(String termSlug, String metaKey, String termInstanceSlug) {
        this.termInstancePK = new TermInstancePK(termSlug, metaKey, termInstanceSlug);
    }

    public TermInstancePK getTermInstancePK() {
        return termInstancePK;
    }

    public void setTermInstancePK(TermInstancePK termInstancePK) {
        this.termInstancePK = termInstancePK;
    }

    public String getTermInstanceValue() {
        return termInstanceValue;
    }

    public void setTermInstanceValue(String termInstanceValue) {
        this.termInstanceValue = termInstanceValue;
    }

    @XmlTransient
    public List<TermInstanceRelations> getTermInstanceRelationsList() {
        return termInstanceRelationsList;
    }

    public void setTermInstanceRelationsList(List<TermInstanceRelations> termInstanceRelationsList) {
        this.termInstanceRelationsList = termInstanceRelationsList;
    }

    public TermMeta getTermMeta() {
        return termMeta;
    }

    public void setTermMeta(TermMeta termMeta) {
        this.termMeta = termMeta;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termInstancePK != null ? termInstancePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermInstance)) {
            return false;
        }
        TermInstance other = (TermInstance) object;
        if ((this.termInstancePK == null && other.termInstancePK != null) || (this.termInstancePK != null && !this.termInstancePK.equals(other.termInstancePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermInstance[ termInstancePK=" + termInstancePK + " ]";
    }
    
}
