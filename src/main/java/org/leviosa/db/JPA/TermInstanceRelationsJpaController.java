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
import org.leviosa.db.entities.TermInstance;
import org.leviosa.db.entities.TermInstanceRelations;
import org.leviosa.db.entities.TermInstanceRelationsPK;

/**
 *
 * @author bhaduri
 */
public class TermInstanceRelationsJpaController implements Serializable {

    public TermInstanceRelationsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TermInstanceRelations termInstanceRelations) throws PreexistingEntityException, Exception {
        if (termInstanceRelations.getTermInstanceRelationsPK() == null) {
            termInstanceRelations.setTermInstanceRelationsPK(new TermInstanceRelationsPK());
        }
        termInstanceRelations.getTermInstanceRelationsPK().setTermMetaKey(termInstanceRelations.getTermInstance().getTermInstancePK().getMetaKey());
        termInstanceRelations.getTermInstanceRelationsPK().setTermSlug(termInstanceRelations.getTermInstance().getTermInstancePK().getTermSlug());
        termInstanceRelations.getTermInstanceRelationsPK().setTermInstanceSlug(termInstanceRelations.getTermInstance().getTermInstancePK().getTermInstanceSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermInstance termInstance = termInstanceRelations.getTermInstance();
            if (termInstance != null) {
                termInstance = em.getReference(termInstance.getClass(), termInstance.getTermInstancePK());
                termInstanceRelations.setTermInstance(termInstance);
            }
            em.persist(termInstanceRelations);
            if (termInstance != null) {
                termInstance.getTermInstanceRelationsList().add(termInstanceRelations);
                termInstance = em.merge(termInstance);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTermInstanceRelations(termInstanceRelations.getTermInstanceRelationsPK()) != null) {
                throw new PreexistingEntityException("TermInstanceRelations " + termInstanceRelations + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TermInstanceRelations termInstanceRelations) throws NonexistentEntityException, Exception {
        termInstanceRelations.getTermInstanceRelationsPK().setTermMetaKey(termInstanceRelations.getTermInstance().getTermInstancePK().getMetaKey());
        termInstanceRelations.getTermInstanceRelationsPK().setTermSlug(termInstanceRelations.getTermInstance().getTermInstancePK().getTermSlug());
        termInstanceRelations.getTermInstanceRelationsPK().setTermInstanceSlug(termInstanceRelations.getTermInstance().getTermInstancePK().getTermInstanceSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermInstanceRelations persistentTermInstanceRelations = em.find(TermInstanceRelations.class, termInstanceRelations.getTermInstanceRelationsPK());
            TermInstance termInstanceOld = persistentTermInstanceRelations.getTermInstance();
            TermInstance termInstanceNew = termInstanceRelations.getTermInstance();
            if (termInstanceNew != null) {
                termInstanceNew = em.getReference(termInstanceNew.getClass(), termInstanceNew.getTermInstancePK());
                termInstanceRelations.setTermInstance(termInstanceNew);
            }
            termInstanceRelations = em.merge(termInstanceRelations);
            if (termInstanceOld != null && !termInstanceOld.equals(termInstanceNew)) {
                termInstanceOld.getTermInstanceRelationsList().remove(termInstanceRelations);
                termInstanceOld = em.merge(termInstanceOld);
            }
            if (termInstanceNew != null && !termInstanceNew.equals(termInstanceOld)) {
                termInstanceNew.getTermInstanceRelationsList().add(termInstanceRelations);
                termInstanceNew = em.merge(termInstanceNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                TermInstanceRelationsPK id = termInstanceRelations.getTermInstanceRelationsPK();
                if (findTermInstanceRelations(id) == null) {
                    throw new NonexistentEntityException("The termInstanceRelations with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(TermInstanceRelationsPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermInstanceRelations termInstanceRelations;
            try {
                termInstanceRelations = em.getReference(TermInstanceRelations.class, id);
                termInstanceRelations.getTermInstanceRelationsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The termInstanceRelations with id " + id + " no longer exists.", enfe);
            }
            TermInstance termInstance = termInstanceRelations.getTermInstance();
            if (termInstance != null) {
                termInstance.getTermInstanceRelationsList().remove(termInstanceRelations);
                termInstance = em.merge(termInstance);
            }
            em.remove(termInstanceRelations);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TermInstanceRelations> findTermInstanceRelationsEntities() {
        return findTermInstanceRelationsEntities(true, -1, -1);
    }

    public List<TermInstanceRelations> findTermInstanceRelationsEntities(int maxResults, int firstResult) {
        return findTermInstanceRelationsEntities(false, maxResults, firstResult);
    }

    private List<TermInstanceRelations> findTermInstanceRelationsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TermInstanceRelations.class));
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

    public TermInstanceRelations findTermInstanceRelations(TermInstanceRelationsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TermInstanceRelations.class, id);
        } finally {
            em.close();
        }
    }

    public int getTermInstanceRelationsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TermInstanceRelations> rt = cq.from(TermInstanceRelations.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
