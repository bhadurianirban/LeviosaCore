/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.hedwig.cloud.response.HedwigResponseCode;
import org.leviosa.db.JPA.TermInstanceRelationsJpaController;
import org.leviosa.db.entities.TermInstanceRelations;

/**
 *
 * @author bhaduri
 */
public class TermInstanceRelationsDAO extends TermInstanceRelationsJpaController {

    public TermInstanceRelationsDAO(EntityManagerFactory emf) {
        super(emf);
    }
//    public int deleteTermInstanceRelations(String termSlug, String termInstanceSlug) {
//        int response;
//        EntityManager em = getEntityManager();
//        EntityTransaction entr = em.getTransaction();
//        Query query = em.createNamedQuery("TermInstanceRelations.deleteAll");
//        query.setParameter("termSlug", termSlug);
//        query.setParameter("termInstanceSlug", termInstanceSlug);
//        entr.begin();
//        int executeUpdate = query.executeUpdate();
//        if (executeUpdate == 0) {
//            response = ResponseCode.DB_NON_EXISTING;
//        } else {
//            response = ResponseCode.SUCCESS;
//        }
//        entr.commit();
//        return response;
//    }

    public List<TermInstanceRelations> getTermInstanceRelations(String termSlug, String termMetaKey, String termInstanceSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstanceRelations> query = em.createNamedQuery("TermInstanceRelations.findByTermInstanceSlug", TermInstanceRelations.class);
        query.setParameter("termSlug", termSlug);
        query.setParameter("termMetaKey", termMetaKey);
        query.setParameter("termInstanceSlug", termInstanceSlug);
        List<TermInstanceRelations> termInstanceRelationsList = query.getResultList();
        return termInstanceRelationsList;
    }

    public List<TermInstanceRelations> getTermInstanceAttachments(String termInstanceRelSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstanceRelations> query = em.createNamedQuery("TermInstanceRelations.findByRelatedTermInstanceSlug", TermInstanceRelations.class);
        query.setParameter("termInstanceRelSlug", termInstanceRelSlug);
        List<TermInstanceRelations> termInstanceRelationsList = query.getResultList();
        return termInstanceRelationsList;
    }

    public List<TermInstanceRelations> getTermInstanceChildren(String parentTermInstanceSlug,String childTermSlug,String childTermMetaKey) {
        EntityManager em = getEntityManager();
        TypedQuery<TermInstanceRelations> query = em.createNamedQuery("TermInstanceRelations.findByParentTermInstanceSlug", TermInstanceRelations.class);
        query.setParameter("parentTermInstanceSlug", parentTermInstanceSlug);
        query.setParameter("childTermSlug", childTermSlug);
        query.setParameter("childTermMetaKey", childTermMetaKey);
        List<TermInstanceRelations> termInstanceRelationsList = query.getResultList();
        return termInstanceRelationsList;
    }

}
