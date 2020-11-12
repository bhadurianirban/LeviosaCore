/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.DAO;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hedwig.cloud.response.HedwigResponseCode;
import org.leviosa.db.JPA.TermJpaController;
import org.leviosa.db.entities.Term;

/**
 *
 * @author dgrf-iv
 */
public class TermDAO extends TermJpaController {

    public TermDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int editTerm(Term term) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term termRem = em.find(Term.class, term.getTermSlug());
            if (termRem != null) {
                em.remove(termRem);

            }
            em.flush();
            em.clear();
            em.persist(term);
            em.getTransaction().commit();
        }catch (Exception ex) {
            Logger.getLogger(TermDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }
    public int createTerm(Term term) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
           
            em.persist(term);
            em.getTransaction().commit();
        }catch (Exception ex) {
            Logger.getLogger(TermDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }
    public int destroyTerm(Term term) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Term termRem = em.find(Term.class, term.getTermSlug());
            if (termRem != null) {
                em.remove(termRem);
            }           
            
            em.getTransaction().commit();
        }catch (Exception ex) {
            Logger.getLogger(TermDAO.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }    
}
