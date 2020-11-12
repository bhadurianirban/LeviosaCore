/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
@Table(name = "term_meta")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TermMeta.findAll", query = "SELECT t FROM TermMeta t")})
public class TermMeta implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TermMetaPK termMetaPK;
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "display_priority")
    private int displayPriority;
    @Basic(optional = false)
    @Column(name = "data_type")
    private String dataType;
    @Column(name = "many_to_one_term_slug")
    private String manyToOneTermSlug;
    @Basic(optional = false)
    @Column(name = "mandatory")
    private boolean mandatory;
    @Lob
    @Column(name = "details")
    private String details;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "termMeta")
    private List<TermInstance> termInstanceList;
    @JoinColumn(name = "term_slug", referencedColumnName = "term_slug", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Term term;

    public TermMeta() {
    }

    public TermMeta(TermMetaPK termMetaPK) {
        this.termMetaPK = termMetaPK;
    }

    public TermMeta(TermMetaPK termMetaPK, int displayPriority, String dataType, boolean mandatory) {
        this.termMetaPK = termMetaPK;
        this.displayPriority = displayPriority;
        this.dataType = dataType;
        this.mandatory = mandatory;
    }

    public TermMeta(String termSlug, String metaKey) {
        this.termMetaPK = new TermMetaPK(termSlug, metaKey);
    }

    public TermMetaPK getTermMetaPK() {
        return termMetaPK;
    }

    public void setTermMetaPK(TermMetaPK termMetaPK) {
        this.termMetaPK = termMetaPK;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDisplayPriority() {
        return displayPriority;
    }

    public void setDisplayPriority(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getManyToOneTermSlug() {
        return manyToOneTermSlug;
    }

    public void setManyToOneTermSlug(String manyToOneTermSlug) {
        this.manyToOneTermSlug = manyToOneTermSlug;
    }

    public boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @XmlTransient
    public List<TermInstance> getTermInstanceList() {
        return termInstanceList;
    }

    public void setTermInstanceList(List<TermInstance> termInstanceList) {
        this.termInstanceList = termInstanceList;
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
        hash += (termMetaPK != null ? termMetaPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TermMeta)) {
            return false;
        }
        TermMeta other = (TermMeta) object;
        if ((this.termMetaPK == null && other.termMetaPK != null) || (this.termMetaPK != null && !this.termMetaPK.equals(other.termMetaPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.TermMeta[ termMetaPK=" + termMetaPK + " ]";
    }
    
}
