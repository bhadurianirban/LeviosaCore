/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.JPA;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.leviosa.db.entities.TermMeta;
import org.leviosa.db.entities.TermInstanceRelations;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.leviosa.db.JPA.exceptions.IllegalOrphanException;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.TermInstance;
import org.leviosa.db.entities.TermInstancePK;

/**
 *
 * @author bhaduri
 */
public class TermInstanceJpaController implements Serializable {

    public TermInstanceJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TermInstance termInstance) throws PreexistingEntityException, Exception {
        if (termInstance.getTermInstancePK() == null) {
            termInstance.setTermInstancePK(new TermInstancePK());
        }
        if (termInstance.getTermInstanceRelationsList() == null) {
            termInstance.setTermInstanceRelationsList(new ArrayList<TermInstanceRelations>());
        }
        termInstance.getTermInstancePK().setTermSlug(termInstance.getTermMeta().getTermMetaPK().getTermSlug());
        termInstance.getTermInstancePK().setMetaKey(termInstance.getTermMeta().getTermMetaPK().getMetaKey());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermMeta termMeta = termInstance.getTermMeta();
            if (termMeta != null) {
                termMeta = em.getReference(termMeta.getClass(), termMeta.getTermMetaPK());
                termInstance.setTermMeta(termMeta);
            }
            List<TermInstanceRelations> attachedTermInstanceRelationsList = new ArrayList<TermInstanceRelations>();
            for (TermInstanceRelations termInstanceRelationsListTermInstanceRelationsToAttach : termInstance.getTermInstanceRelationsList()) {
                termInstanceRelationsListTermInstanceRelationsToAttach = em.getReference(termInstanceRelationsListTermInstanceRelationsToAttach.getClass(), termInstanceRelationsListTermInstanceRelationsToAttach.getTermInstanceRelationsPK());
                attachedTermInstanceRelationsList.add(termInstanceRelationsListTermInstanceRelationsToAttach);
            }
            termInstance.setTermInstanceRelationsList(attachedTermInstanceRelationsList);
            em.persist(termInstance);
            if (termMeta != null) {
                termMeta.getTermInstanceList().add(termInstance);
                termMeta = em.merge(termMeta);
            }
            for (TermInstanceRelations termInstanceRelationsListTermInstanceRelations : termInstance.getTermInstanceRelationsList()) {
                TermInstance oldTermInstanceOfTermInstanceRelationsListTermInstanceRelations = termInstanceRelationsListTermInstanceRelations.getTermInstance();
                termInstanceRelationsListTermInstanceRelations.setTermInstance(termInstance);
                termInstanceRelationsListTermInstanceRelations = em.merge(termInstanceRelationsListTermInstanceRelations);
                if (oldTermInstanceOfTermInstanceRelationsListTermInstanceRelations != null) {
                    oldTermInstanceOfTermInstanceRelationsListTermInstanceRelations.getTermInstanceRelationsList().remove(termInstanceRelationsListTermInstanceRelations);
                    oldTermInstanceOfTermInstanceRelationsListTermInstanceRelations = em.merge(oldTermInstanceOfTermInstanceRelationsListTermInstanceRelations);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTermInstance(termInstance.getTermInstancePK()) != null) {
                throw new PreexistingEntityException("TermInstance " + termInstance + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TermInstance termInstance) throws IllegalOrphanException, NonexistentEntityException, Exception {
        termInstance.getTermInstancePK().setTermSlug(termInstance.getTermMeta().getTermMetaPK().getTermSlug());
        termInstance.getTermInstancePK().setMetaKey(termInstance.getTermMeta().getTermMetaPK().getMetaKey());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermInstance persistentTermInstance = em.find(TermInstance.class, termInstance.getTermInstancePK());
            TermMeta termMetaOld = persistentTermInstance.getTermMeta();
            TermMeta termMetaNew = termInstance.getTermMeta();
            List<TermInstanceRelations> termInstanceRelationsListOld = persistentTermInstance.getTermInstanceRelationsList();
            List<TermInstanceRelations> termInstanceRelationsListNew = termInstance.getTermInstanceRelationsList();
            List<String> illegalOrphanMessages = null;
            for (TermInstanceRelations termInstanceRelationsListOldTermInstanceRelations : termInstanceRelationsListOld) {
                if (!termInstanceRelationsListNew.contains(termInstanceRelationsListOldTermInstanceRelations)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TermInstanceRelations " + termInstanceRelationsListOldTermInstanceRelations + " since its termInstance field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (termMetaNew != null) {
                termMetaNew = em.getReference(termMetaNew.getClass(), termMetaNew.getTermMetaPK());
                termInstance.setTermMeta(termMetaNew);
            }
            List<TermInstanceRelations> attachedTermInstanceRelationsListNew = new ArrayList<TermInstanceRelations>();
            for (TermInstanceRelations termInstanceRelationsListNewTermInstanceRelationsToAttach : termInstanceRelationsListNew) {
                termInstanceRelationsListNewTermInstanceRelationsToAttach = em.getReference(termInstanceRelationsListNewTermInstanceRelationsToAttach.getClass(), termInstanceRelationsListNewTermInstanceRelationsToAttach.getTermInstanceRelationsPK());
                attachedTermInstanceRelationsListNew.add(termInstanceRelationsListNewTermInstanceRelationsToAttach);
            }
            termInstanceRelationsListNew = attachedTermInstanceRelationsListNew;
            termInstance.setTermInstanceRelationsList(termInstanceRelationsListNew);
            termInstance = em.merge(termInstance);
            if (termMetaOld != null && !termMetaOld.equals(termMetaNew)) {
                termMetaOld.getTermInstanceList().remove(termInstance);
                termMetaOld = em.merge(termMetaOld);
            }
            if (termMetaNew != null && !termMetaNew.equals(termMetaOld)) {
                termMetaNew.getTermInstanceList().add(termInstance);
                termMetaNew = em.merge(termMetaNew);
            }
            for (TermInstanceRelations termInstanceRelationsListNewTermInstanceRelations : termInstanceRelationsListNew) {
                if (!termInstanceRelationsListOld.contains(termInstanceRelationsListNewTermInstanceRelations)) {
                    TermInstance oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations = termInstanceRelationsListNewTermInstanceRelations.getTermInstance();
                    termInstanceRelationsListNewTermInstanceRelations.setTermInstance(termInstance);
                    termInstanceRelationsListNewTermInstanceRelations = em.merge(termInstanceRelationsListNewTermInstanceRelations);
                    if (oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations != null && !oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations.equals(termInstance)) {
                        oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations.getTermInstanceRelationsList().remove(termInstanceRelationsListNewTermInstanceRelations);
                        oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations = em.merge(oldTermInstanceOfTermInstanceRelationsListNewTermInstanceRelations);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                TermInstancePK id = termInstance.getTermInstancePK();
                if (findTermInstance(id) == null) {
                    throw new NonexistentEntityException("The termInstance with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(TermInstancePK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermInstance termInstance;
            try {
                termInstance = em.getReference(TermInstance.class, id);
                termInstance.getTermInstancePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The termInstance with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<TermInstanceRelations> termInstanceRelationsListOrphanCheck = termInstance.getTermInstanceRelationsList();
            for (TermInstanceRelations termInstanceRelationsListOrphanCheckTermInstanceRelations : termInstanceRelationsListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TermInstance (" + termInstance + ") cannot be destroyed since the TermInstanceRelations " + termInstanceRelationsListOrphanCheckTermInstanceRelations + " in its termInstanceRelationsList field has a non-nullable termInstance field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            TermMeta termMeta = termInstance.getTermMeta();
            if (termMeta != null) {
                termMeta.getTermInstanceList().remove(termInstance);
                termMeta = em.merge(termMeta);
            }
            em.remove(termInstance);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TermInstance> findTermInstanceEntities() {
        return findTermInstanceEntities(true, -1, -1);
    }

    public List<TermInstance> findTermInstanceEntities(int maxResults, int firstResult) {
        return findTermInstanceEntities(false, maxResults, firstResult);
    }

    private List<TermInstance> findTermInstanceEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TermInstance.class));
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

    public TermInstance findTermInstance(TermInstancePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TermInstance.class, id);
        } finally {
            em.close();
        }
    }

    public int getTermInstanceCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TermInstance> rt = cq.from(TermInstance.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
