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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.leviosa.db.JPA.exceptions.IllegalOrphanException;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.Term;
import org.leviosa.db.entities.TermRole;

/**
 *
 * @author bhaduri
 */
public class TermJpaController implements Serializable {

    public TermJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Term term) throws PreexistingEntityException, Exception {
        if (term.getTermMetaList() == null) {
            term.setTermMetaList(new ArrayList<TermMeta>());
        }
        if (term.getTermRoleList() == null) {
            term.setTermRoleList(new ArrayList<TermRole>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<TermMeta> attachedTermMetaList = new ArrayList<TermMeta>();
            for (TermMeta termMetaListTermMetaToAttach : term.getTermMetaList()) {
                termMetaListTermMetaToAttach = em.getReference(termMetaListTermMetaToAttach.getClass(), termMetaListTermMetaToAttach.getTermMetaPK());
                attachedTermMetaList.add(termMetaListTermMetaToAttach);
            }
            term.setTermMetaList(attachedTermMetaList);
            List<TermRole> attachedTermRoleList = new ArrayList<TermRole>();
            for (TermRole termRoleListTermRoleToAttach : term.getTermRoleList()) {
                termRoleListTermRoleToAttach = em.getReference(termRoleListTermRoleToAttach.getClass(), termRoleListTermRoleToAttach.getTermRolePK());
                attachedTermRoleList.add(termRoleListTermRoleToAttach);
            }
            term.setTermRoleList(attachedTermRoleList);
            em.persist(term);
            for (TermMeta termMetaListTermMeta : term.getTermMetaList()) {
                Term oldTermOfTermMetaListTermMeta = termMetaListTermMeta.getTerm();
                termMetaListTermMeta.setTerm(term);
                termMetaListTermMeta = em.merge(termMetaListTermMeta);
                if (oldTermOfTermMetaListTermMeta != null) {
                    oldTermOfTermMetaListTermMeta.getTermMetaList().remove(termMetaListTermMeta);
                    oldTermOfTermMetaListTermMeta = em.merge(oldTermOfTermMetaListTermMeta);
                }
            }
            for (TermRole termRoleListTermRole : term.getTermRoleList()) {
                Term oldTermOfTermRoleListTermRole = termRoleListTermRole.getTerm();
                termRoleListTermRole.setTerm(term);
                termRoleListTermRole = em.merge(termRoleListTermRole);
                if (oldTermOfTermRoleListTermRole != null) {
                    oldTermOfTermRoleListTermRole.getTermRoleList().remove(termRoleListTermRole);
                    oldTermOfTermRoleListTermRole = em.merge(oldTermOfTermRoleListTermRole);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTerm(term.getTermSlug()) != null) {
                throw new PreexistingEntityException("Term " + term + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Term term) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term persistentTerm = em.find(Term.class, term.getTermSlug());
            List<TermMeta> termMetaListOld = persistentTerm.getTermMetaList();
            List<TermMeta> termMetaListNew = term.getTermMetaList();
            List<TermRole> termRoleListOld = persistentTerm.getTermRoleList();
            List<TermRole> termRoleListNew = term.getTermRoleList();
            List<String> illegalOrphanMessages = null;
            for (TermMeta termMetaListOldTermMeta : termMetaListOld) {
                if (!termMetaListNew.contains(termMetaListOldTermMeta)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TermMeta " + termMetaListOldTermMeta + " since its term field is not nullable.");
                }
            }
            for (TermRole termRoleListOldTermRole : termRoleListOld) {
                if (!termRoleListNew.contains(termRoleListOldTermRole)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TermRole " + termRoleListOldTermRole + " since its term field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<TermMeta> attachedTermMetaListNew = new ArrayList<TermMeta>();
            for (TermMeta termMetaListNewTermMetaToAttach : termMetaListNew) {
                termMetaListNewTermMetaToAttach = em.getReference(termMetaListNewTermMetaToAttach.getClass(), termMetaListNewTermMetaToAttach.getTermMetaPK());
                attachedTermMetaListNew.add(termMetaListNewTermMetaToAttach);
            }
            termMetaListNew = attachedTermMetaListNew;
            term.setTermMetaList(termMetaListNew);
            List<TermRole> attachedTermRoleListNew = new ArrayList<TermRole>();
            for (TermRole termRoleListNewTermRoleToAttach : termRoleListNew) {
                termRoleListNewTermRoleToAttach = em.getReference(termRoleListNewTermRoleToAttach.getClass(), termRoleListNewTermRoleToAttach.getTermRolePK());
                attachedTermRoleListNew.add(termRoleListNewTermRoleToAttach);
            }
            termRoleListNew = attachedTermRoleListNew;
            term.setTermRoleList(termRoleListNew);
            term = em.merge(term);
            for (TermMeta termMetaListNewTermMeta : termMetaListNew) {
                if (!termMetaListOld.contains(termMetaListNewTermMeta)) {
                    Term oldTermOfTermMetaListNewTermMeta = termMetaListNewTermMeta.getTerm();
                    termMetaListNewTermMeta.setTerm(term);
                    termMetaListNewTermMeta = em.merge(termMetaListNewTermMeta);
                    if (oldTermOfTermMetaListNewTermMeta != null && !oldTermOfTermMetaListNewTermMeta.equals(term)) {
                        oldTermOfTermMetaListNewTermMeta.getTermMetaList().remove(termMetaListNewTermMeta);
                        oldTermOfTermMetaListNewTermMeta = em.merge(oldTermOfTermMetaListNewTermMeta);
                    }
                }
            }
            for (TermRole termRoleListNewTermRole : termRoleListNew) {
                if (!termRoleListOld.contains(termRoleListNewTermRole)) {
                    Term oldTermOfTermRoleListNewTermRole = termRoleListNewTermRole.getTerm();
                    termRoleListNewTermRole.setTerm(term);
                    termRoleListNewTermRole = em.merge(termRoleListNewTermRole);
                    if (oldTermOfTermRoleListNewTermRole != null && !oldTermOfTermRoleListNewTermRole.equals(term)) {
                        oldTermOfTermRoleListNewTermRole.getTermRoleList().remove(termRoleListNewTermRole);
                        oldTermOfTermRoleListNewTermRole = em.merge(oldTermOfTermRoleListNewTermRole);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = term.getTermSlug();
                if (findTerm(id) == null) {
                    throw new NonexistentEntityException("The term with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term term;
            try {
                term = em.getReference(Term.class, id);
                term.getTermSlug();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The term with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<TermMeta> termMetaListOrphanCheck = term.getTermMetaList();
            for (TermMeta termMetaListOrphanCheckTermMeta : termMetaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Term (" + term + ") cannot be destroyed since the TermMeta " + termMetaListOrphanCheckTermMeta + " in its termMetaList field has a non-nullable term field.");
            }
            List<TermRole> termRoleListOrphanCheck = term.getTermRoleList();
            for (TermRole termRoleListOrphanCheckTermRole : termRoleListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Term (" + term + ") cannot be destroyed since the TermRole " + termRoleListOrphanCheckTermRole + " in its termRoleList field has a non-nullable term field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(term);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Term> findTermEntities() {
        return findTermEntities(true, -1, -1);
    }

    public List<Term> findTermEntities(int maxResults, int firstResult) {
        return findTermEntities(false, maxResults, firstResult);
    }

    private List<Term> findTermEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Term.class));
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

    public Term findTerm(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Term.class, id);
        } finally {
            em.close();
        }
    }

    public int getTermCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Term> rt = cq.from(Term.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
