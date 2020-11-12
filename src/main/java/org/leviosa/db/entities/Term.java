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
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "term")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Term.findAll", query = "SELECT t FROM Term t")})
public class Term implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "term_slug")
    private String termSlug;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "screen")
    private String screen;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "term")
    private List<TermMeta> termMetaList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "term")
    private List<TermRole> termRoleList;

    public Term() {
    }

    public Term(String termSlug) {
        this.termSlug = termSlug;
    }

    public Term(String termSlug, String name, String screen) {
        this.termSlug = termSlug;
        this.name = name;
        this.screen = screen;
    }

    public String getTermSlug() {
        return termSlug;
    }

    public void setTermSlug(String termSlug) {
        this.termSlug = termSlug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    @XmlTransient
    public List<TermMeta> getTermMetaList() {
        return termMetaList;
    }

    public void setTermMetaList(List<TermMeta> termMetaList) {
        this.termMetaList = termMetaList;
    }

    @XmlTransient
    public List<TermRole> getTermRoleList() {
        return termRoleList;
    }

    public void setTermRoleList(List<TermRole> termRoleList) {
        this.termRoleList = termRoleList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (termSlug != null ? termSlug.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Term)) {
            return false;
        }
        Term other = (Term) object;
        if ((this.termSlug == null && other.termSlug != null) || (this.termSlug != null && !this.termSlug.equals(other.termSlug))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.testdb.Term[ termSlug=" + termSlug + " ]";
    }
    
}
