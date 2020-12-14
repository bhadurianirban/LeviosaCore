/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.bl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.hedwig.cloud.response.HedwigResponseCode;
import org.hedwig.cms.dto.MenuDTO;
import org.hedwig.cms.dto.MenuNode;
import org.leviosa.db.DAO.MenuDAO;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.Menu;

/**
 *
 * @author bhaduri
 */
public class MenuService {



    public List<HashMap<String, String>> getMenuListInMap() {

        List<Menu> menuList = getMenuList();

        List<HashMap<String, String>> menuMapList = menuList.stream().map((menu) -> {
            HashMap<String, String> menuMap = new HashMap<>();
            menuMap.put("id", menu.getId().toString());
            menuMap.put("name", menu.getName());
            menuMap.put("term", menu.getTermSlug());
            Integer parent = menu.getParent();
            String hasChild = menuHasChild(menu.getId());
            menuMap.put("hasChild", hasChild);
            if (parent == null) {
                menuMap.put("parent", null);
                String parentManuName = "";
                menuMap.put("parentName", parentManuName);
            } else {
                menuMap.put("parent", menu.getParent().toString());
                String parentManuName = getMenu(parent).getName();
                menuMap.put("parentName", parentManuName);
            }
            return menuMap;
        }).collect(Collectors.toList());
        return menuMapList;

    }

    private String menuHasChild(int menuId) {
        List<Menu> menuList = getMenuList();
        if (menuList.isEmpty()) {
            return "no";
        } else {
            List<Menu> menuListChild = menuList.stream().filter(m -> Objects.equals(m.getParent(), menuId)).collect(Collectors.toList());
            if (menuListChild.isEmpty()) {
                return "no";
            } else {
                return "yes";
            }
        }
    }

    public MenuDTO getMenuTree(MenuDTO menuDTO) {

        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);
        List<Menu> menuList = menuDAO.findMenuEntities();

        MenuMaker menuMaker = new MenuMaker(menuDTO.getHedwigAuthCredentials());
        MenuNode authorisedMenuRoot = menuMaker.getUserMenu(menuList);
        menuDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        menuDTO.setRootMenuNode(authorisedMenuRoot);
        return menuDTO;
    }

    public List<Menu> getMenuList() {

        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);
        List<Menu> menuList = menuDAO.findMenuEntities();
        

        return menuList;
    }

    public int insertMenu(Menu menu) {
        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);

        try {
            menuDAO.create(menu);
        } catch (PreexistingEntityException e) {
            
            return HedwigResponseCode.DB_DUPLICATE;
        } catch (Exception ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            
            return HedwigResponseCode.DB_SEVERE;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public int deleteMenu(String menuId) {
        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);

        try {
            menuDAO.destroy(Integer.parseInt(menuId));
            return HedwigResponseCode.SUCCESS;
        } catch (NonexistentEntityException e) {
            
            return HedwigResponseCode.DB_NON_EXISTING;
        } catch (NumberFormatException ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
            
        } catch (Exception ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
            
        }

    }

    public int editMenu(Menu menu) {
        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);

        try {
            menuDAO.edit(menu);
            return HedwigResponseCode.SUCCESS;
        } catch (NonexistentEntityException e) {
            return HedwigResponseCode.DB_NON_EXISTING;
        } catch (Exception ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }

    }

    public Menu getMenu(int id) {
        MenuDAO menuDAO = new MenuDAO(DatabaseConnection.EMF);
        Menu menu = menuDAO.findMenu(id);
        return menu;

    }
}
