/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.JPA;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.Term;
import org.leviosa.db.entities.TermRole;
import org.leviosa.db.entities.TermRolePK;

/**
 *
 * @author bhaduri
 */
public class TermRoleJpaController implements Serializable {

    public TermRoleJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TermRole termRole) throws PreexistingEntityException, Exception {
        if (termRole.getTermRolePK() == null) {
            termRole.setTermRolePK(new TermRolePK());
        }
        termRole.getTermRolePK().setTermSlug(termRole.getTerm().getTermSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term term = termRole.getTerm();
            if (term != null) {
                term = em.getReference(term.getClass(), term.getTermSlug());
                termRole.setTerm(term);
            }
            em.persist(termRole);
            if (term != null) {
                term.getTermRoleList().add(termRole);
                term = em.merge(term);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTermRole(termRole.getTermRolePK()) != null) {
                throw new PreexistingEntityException("TermRole " + termRole + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TermRole termRole) throws NonexistentEntityException, Exception {
        termRole.getTermRolePK().setTermSlug(termRole.getTerm().getTermSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermRole persistentTermRole = em.find(TermRole.class, termRole.getTermRolePK());
            Term termOld = persistentTermRole.getTerm();
            Term termNew = termRole.getTerm();
            if (termNew != null) {
                termNew = em.getReference(termNew.getClass(), termNew.getTermSlug());
                termRole.setTerm(termNew);
            }
            termRole = em.merge(termRole);
            if (termOld != null && !termOld.equals(termNew)) {
                termOld.getTermRoleList().remove(termRole);
                termOld = em.merge(termOld);
            }
            if (termNew != null && !termNew.equals(termOld)) {
                termNew.getTermRoleList().add(termRole);
                termNew = em.merge(termNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                TermRolePK id = termRole.getTermRolePK();
                if (findTermRole(id) == null) {
                    throw new NonexistentEntityException("The termRole with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(TermRolePK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermRole termRole;
            try {
                termRole = em.getReference(TermRole.class, id);
                termRole.getTermRolePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The termRole with id " + id + " no longer exists.", enfe);
            }
            Term term = termRole.getTerm();
            if (term != null) {
                term.getTermRoleList().remove(termRole);
                term = em.merge(term);
            }
            em.remove(termRole);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TermRole> findTermRoleEntities() {
        return findTermRoleEntities(true, -1, -1);
    }

    public List<TermRole> findTermRoleEntities(int maxResults, int firstResult) {
        return findTermRoleEntities(false, maxResults, firstResult);
    }

    private List<TermRole> findTermRoleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TermRole.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public TermRole findTermRole(TermRolePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TermRole.class, id);
        } finally {
            em.close();
        }
    }

    public int getTermRoleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TermRole> rt = cq.from(TermRole.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
