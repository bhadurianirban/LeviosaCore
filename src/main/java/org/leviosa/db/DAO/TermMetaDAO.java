/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.DAO;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.hedwig.cloud.response.HedwigResponseCode;
import org.leviosa.db.JPA.TermMetaJpaController;
import org.leviosa.db.entities.TermMeta;

public class TermMetaDAO extends TermMetaJpaController {

    public TermMetaDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int destroyTermMeta(TermMeta termMeta) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();

            TermMeta termMetaRem = em.find(TermMeta.class, termMeta.getTermMetaPK());
            em.remove(termMetaRem);
            em.getTransaction().commit();
        } catch (Exception ex) {
            Logger.getLogger(TermMetaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public List<TermMeta> getOrderedTermMetaList(String termSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<TermMeta> query = em.createNamedQuery("TermMeta.findOrderedMeta", TermMeta.class);
        query.setParameter("termSlug", termSlug);
        List<TermMeta> termMetas = query.getResultList();
        return termMetas;
    }

    public List<TermMeta> getTermMetaListForSelectOneTerm(String attachedTermSlug, String dataType) {
        EntityManager em = getEntityManager();
        TypedQuery<TermMeta> query = em.createNamedQuery("TermMeta.findBySelectOne", TermMeta.class);
        query.setParameter("manyToOneTerm", attachedTermSlug);
        query.setParameter("dataType", dataType);
        List<TermMeta> termMetas = query.getResultList();
        return termMetas;
    }

    public int getMaxDisplayPriority(String termSlug) {
        EntityManager em = getEntityManager();
        int m;
        try {
            TypedQuery<Integer> query = em.createNamedQuery("TermMeta.findMaxDisplayPriority", Integer.class);
            query.setParameter("termSlug", termSlug);
            m = query.getSingleResult();
            return m;
        } catch (NullPointerException e) {
            return -1;
        }
    }

}
