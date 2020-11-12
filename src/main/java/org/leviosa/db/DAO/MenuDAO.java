/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.db.DAO;

import javax.persistence.EntityManagerFactory;
import org.leviosa.db.JPA.MenuJpaController;

/**
 *
 * @author bhaduri
 */
public class MenuDAO extends MenuJpaController{

    public MenuDAO(EntityManagerFactory emf) {
        super(emf);
    }
    
    
}
