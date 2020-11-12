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
import org.leviosa.db.entities.Term;
import org.leviosa.db.entities.TermInstance;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.leviosa.db.JPA.exceptions.IllegalOrphanException;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.TermMeta;
import org.leviosa.db.entities.TermMetaPK;

/**
 *
 * @author bhaduri
 */
public class TermMetaJpaController implements Serializable {

    public TermMetaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TermMeta termMeta) throws PreexistingEntityException, Exception {
        if (termMeta.getTermMetaPK() == null) {
            termMeta.setTermMetaPK(new TermMetaPK());
        }
        if (termMeta.getTermInstanceList() == null) {
            termMeta.setTermInstanceList(new ArrayList<TermInstance>());
        }
        termMeta.getTermMetaPK().setTermSlug(termMeta.getTerm().getTermSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term term = termMeta.getTerm();
            if (term != null) {
                term = em.getReference(term.getClass(), term.getTermSlug());
                termMeta.setTerm(term);
            }
            List<TermInstance> attachedTermInstanceList = new ArrayList<TermInstance>();
            for (TermInstance termInstanceListTermInstanceToAttach : termMeta.getTermInstanceList()) {
                termInstanceListTermInstanceToAttach = em.getReference(termInstanceListTermInstanceToAttach.getClass(), termInstanceListTermInstanceToAttach.getTermInstancePK());
                attachedTermInstanceList.add(termInstanceListTermInstanceToAttach);
            }
            termMeta.setTermInstanceList(attachedTermInstanceList);
            em.persist(termMeta);
            if (term != null) {
                term.getTermMetaList().add(termMeta);
                term = em.merge(term);
            }
            for (TermInstance termInstanceListTermInstance : termMeta.getTermInstanceList()) {
                TermMeta oldTermMetaOfTermInstanceListTermInstance = termInstanceListTermInstance.getTermMeta();
                termInstanceListTermInstance.setTermMeta(termMeta);
                termInstanceListTermInstance = em.merge(termInstanceListTermInstance);
                if (oldTermMetaOfTermInstanceListTermInstance != null) {
                    oldTermMetaOfTermInstanceListTermInstance.getTermInstanceList().remove(termInstanceListTermInstance);
                    oldTermMetaOfTermInstanceListTermInstance = em.merge(oldTermMetaOfTermInstanceListTermInstance);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTermMeta(termMeta.getTermMetaPK()) != null) {
                throw new PreexistingEntityException("TermMeta " + termMeta + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TermMeta termMeta) throws IllegalOrphanException, NonexistentEntityException, Exception {
        termMeta.getTermMetaPK().setTermSlug(termMeta.getTerm().getTermSlug());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermMeta persistentTermMeta = em.find(TermMeta.class, termMeta.getTermMetaPK());
            Term termOld = persistentTermMeta.getTerm();
            Term termNew = termMeta.getTerm();
            List<TermInstance> termInstanceListOld = persistentTermMeta.getTermInstanceList();
            List<TermInstance> termInstanceListNew = termMeta.getTermInstanceList();
            List<String> illegalOrphanMessages = null;
            for (TermInstance termInstanceListOldTermInstance : termInstanceListOld) {
                if (!termInstanceListNew.contains(termInstanceListOldTermInstance)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TermInstance " + termInstanceListOldTermInstance + " since its termMeta field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (termNew != null) {
                termNew = em.getReference(termNew.getClass(), termNew.getTermSlug());
                termMeta.setTerm(termNew);
            }
            List<TermInstance> attachedTermInstanceListNew = new ArrayList<TermInstance>();
            for (TermInstance termInstanceListNewTermInstanceToAttach : termInstanceListNew) {
                termInstanceListNewTermInstanceToAttach = em.getReference(termInstanceListNewTermInstanceToAttach.getClass(), termInstanceListNewTermInstanceToAttach.getTermInstancePK());
                attachedTermInstanceListNew.add(termInstanceListNewTermInstanceToAttach);
            }
            termInstanceListNew = attachedTermInstanceListNew;
            termMeta.setTermInstanceList(termInstanceListNew);
            termMeta = em.merge(termMeta);
            if (termOld != null && !termOld.equals(termNew)) {
                termOld.getTermMetaList().remove(termMeta);
                termOld = em.merge(termOld);
            }
            if (termNew != null && !termNew.equals(termOld)) {
                termNew.getTermMetaList().add(termMeta);
                termNew = em.merge(termNew);
            }
            for (TermInstance termInstanceListNewTermInstance : termInstanceListNew) {
                if (!termInstanceListOld.contains(termInstanceListNewTermInstance)) {
                    TermMeta oldTermMetaOfTermInstanceListNewTermInstance = termInstanceListNewTermInstance.getTermMeta();
                    termInstanceListNewTermInstance.setTermMeta(termMeta);
                    termInstanceListNewTermInstance = em.merge(termInstanceListNewTermInstance);
                    if (oldTermMetaOfTermInstanceListNewTermInstance != null && !oldTermMetaOfTermInstanceListNewTermInstance.equals(termMeta)) {
                        oldTermMetaOfTermInstanceListNewTermInstance.getTermInstanceList().remove(termInstanceListNewTermInstance);
                        oldTermMetaOfTermInstanceListNewTermInstance = em.merge(oldTermMetaOfTermInstanceListNewTermInstance);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                TermMetaPK id = termMeta.getTermMetaPK();
                if (findTermMeta(id) == null) {
                    throw new NonexistentEntityException("The termMeta with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(TermMetaPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TermMeta termMeta;
            try {
                termMeta = em.getReference(TermMeta.class, id);
                termMeta.getTermMetaPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The termMeta with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<TermInstance> termInstanceListOrphanCheck = termMeta.getTermInstanceList();
            for (TermInstance termInstanceListOrphanCheckTermInstance : termInstanceListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TermMeta (" + termMeta + ") cannot be destroyed since the TermInstance " + termInstanceListOrphanCheckTermInstance + " in its termInstanceList field has a non-nullable termMeta field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Term term = termMeta.getTerm();
            if (term != null) {
                term.getTermMetaList().remove(termMeta);
                term = em.merge(term);
            }
            em.remove(termMeta);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TermMeta> findTermMetaEntities() {
        return findTermMetaEntities(true, -1, -1);
    }

    public List<TermMeta> findTermMetaEntities(int maxResults, int firstResult) {
        return findTermMetaEntities(false, maxResults, firstResult);
    }

    private List<TermMeta> findTermMetaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TermMeta.class));
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

    public TermMeta findTermMeta(TermMetaPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TermMeta.class, id);
        } finally {
            em.close();
        }
    }

    public int getTermMetaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TermMeta> rt = cq.from(TermMeta.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
