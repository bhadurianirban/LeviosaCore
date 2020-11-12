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
import org.leviosa.db.JPA.TermInstanceJpaController;
import org.leviosa.db.entities.TermInstance;

/**
 *
 * @author dgrf-iv
 */
public class TermInstanceDAO extends TermInstanceJpaController {

    public TermInstanceDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int editTermInstance(List<TermInstance> termInstanceList) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            for (TermInstance termInstance : termInstanceList) {
                TermInstance termInstanceRem = em.find(TermInstance.class, termInstance.getTermInstancePK());
                if (termInstanceRem != null) {
                    em.remove(termInstanceRem);

                }
                em.flush();
                em.clear();
                em.persist(termInstance);
            }

            em.getTransaction().commit();
        } catch (Exception ex) {
            Logger.getLogger(TermInstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public int createTermInstance(List<TermInstance> termInstanceList) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            for (TermInstance termInstance : termInstanceList) {
                em.persist(termInstance);
            }

            em.getTransaction().commit();
        } catch (Exception ex) {
            Logger.getLogger(TermInstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public int destroyTermInstance(List<TermInstance> termInstanceList) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            for (TermInstance termInstance : termInstanceList) {
                TermInstance termInstanceRem = em.find(TermInstance.class, termInstance.getTermInstancePK());
                em.remove(termInstanceRem);

            }

            em.getTransaction().commit();
        } catch (Exception ex) {
            Logger.getLogger(TermInstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public List<TermInstance> getTermMetaData(String termSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstance> query = em.createNamedQuery("TermMetaData.findByTermMetaTermSlug", TermInstance.class);
        query.setParameter("termMetaTermSlug", termSlug);
        List<TermInstance> termMetaDatas = query.getResultList();
        return termMetaDatas;
    }

    public List<String> getDistinctTermMetaDataSlugs(String termSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<String> query = em.createNamedQuery("TermMetaData.findDistinctInstanceSlugs", String.class);
        query.setParameter("termSlug", termSlug);
        List<String> distinctTermInstances = query.getResultList();
        return distinctTermInstances;
    }



    public List<TermInstance> getTermInstance(String termSlug, String termInstanceSlug) {
        EntityManager em = getEntityManager();
        //TypedQuery<TermMetaData> query = em.createNamedQuery("TermMetaData.findByTermInstanceSlugAndTermSlug", TermMetaData.class);
        TypedQuery<TermInstance> query = em.createNamedQuery("TermMetaData.findByMetaDataDisplayOrder", TermInstance.class);
        query.setParameter("termMetaTermSlug", termSlug);
        query.setParameter("termInstanceSlug", termInstanceSlug);
        List<TermInstance> termMetaDatas = query.getResultList();
        return termMetaDatas;
    }
    public List<TermInstance> getMetaDataBySlugAndMetakey(String termSlug, String termMetaKey) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstance> query = em.createNamedQuery("TermMetaData.findByTermMetaKeyAndTermSlug", TermInstance.class);
        query.setParameter("termMetaTermSlug", termSlug);
        query.setParameter("termMetaKey", termMetaKey);
        List<TermInstance> termMetaDatas = query.getResultList();
        return termMetaDatas;
    }
    public List<TermInstance> getTermInstanceChildren(String parentTermInstanceSlug,String childTermSlug,String childTermMetaKey) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstance> query = em.createNamedQuery("TermInstance.findChildTermInstance", TermInstance.class);
        query.setParameter("parentTermInstanceSlug", parentTermInstanceSlug);
        query.setParameter("childTermSlug", childTermSlug);
        query.setParameter("childTermMetaKey", childTermMetaKey);
        List<TermInstance> termInstanceList = query.getResultList();
        return termInstanceList;
    }

    public List<TermInstance> getTermInstanceByInstanceSluug(String termInstanceSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstance> query = em.createNamedQuery("TermMetaData.findByTermInstanceSlug", TermInstance.class);
        query.setParameter("termInstanceSlug", termInstanceSlug);
        List<TermInstance> termInstanceList = query.getResultList();
        return termInstanceList;
    }


}
