/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.bl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hedwig.cloud.dto.HedwigAuthCredentials;
import org.hedwig.cloud.response.HedwigResponseCode;

import org.hedwig.cms.constants.CMSConstants;

import org.hedwig.cms.dto.MenuNode;
import org.hedwig.cms.dto.TermDTO;
import org.leviosa.db.entities.Menu;

/**
 *
 * @author bhaduri
 */
public class MenuMaker {

    private final HedwigAuthCredentials AUTH_CREDENTIALS;

    public MenuMaker(HedwigAuthCredentials authCredentials) {
        this.AUTH_CREDENTIALS = authCredentials;

    }

    public MenuNode getUserMenu(List<Menu> menuList) {

        List<MenuNode> menuNodeList = menuList.stream().map(m -> {
            MenuNode menuNode = new MenuNode();
            menuNode.setId(m.getId());
            menuNode.setName(m.getName());
            menuNode.setParent(m.getParent());
            menuNode.setTermSlug(m.getTermSlug());
            return menuNode;
        }).collect(Collectors.toList());
        CMSService cmss = new CMSService();
        for (MenuNode menuNode : menuNodeList) {
            TermDTO termDTO = new TermDTO();
            termDTO.setAuthCredentials(AUTH_CREDENTIALS);
            termDTO.setTermSlug(menuNode.getTermSlug());
            termDTO = cmss.getTermDetails(termDTO);
            Map<String, Object> term = termDTO.getTermDetails();
            String termName = (String) term.get(CMSConstants.TERM_NAME);
            String termUrl = (String) term.get(CMSConstants.TERM_SCREEN);
            menuNode.setTermName(termName);
            menuNode.setTermUrl(termUrl);
        }
        Map<Integer, MenuNode> menuMap = menuNodeList.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

        List<MenuNode> menuForest = getMenuForest(menuMap, menuNodeList);
        MenuNode authorisedMenuRoot = new MenuNode();
        createAuthorisedMenu(menuForest, authorisedMenuRoot);

        return authorisedMenuRoot;
    }


    /**
     * Attaches the children nodes to the parent nodes Returns the list of root
     * nodes of the constructed forest
     */
    List<MenuNode> getMenuForest(Map<Integer, MenuNode> menuMap, List<MenuNode> menuNodeList) {

        List<MenuNode> roots = new ArrayList<>();

        for (MenuNode item : menuNodeList) {
            if (item.getParent() == null) {
                roots.add(item);
            } else {
                MenuNode parent = menuMap.get(item.getParent());
                if (parent.getChildren() == null) {
                    List<MenuNode> children = new ArrayList<>();
                    parent.setChildren(children);
                }
                parent.getChildren().add(item);
            }
        }

        return roots;
    }

    private boolean isMenuNodeAuthorised(MenuNode menuNode) {

        List<MenuNode> leafMenuNodeList = new ArrayList<>();
        getLeafMenuList(leafMenuNodeList, menuNode);
        for (MenuNode leafMenuNode : leafMenuNodeList) {

            boolean menuNodeAuthorised = isLeafNodeAuthorised(leafMenuNode);

            if (menuNodeAuthorised) {
                return true;
            }
        }

        return false;
    }


    private void createAuthorisedMenu(List<MenuNode> menuForest, MenuNode authorisedMenuRoot) {
        List<MenuNode> children = new ArrayList<>();
        for (MenuNode menuNode : menuForest) {
            if (menuNode.getChildren() == null) {
                if (isLeafNodeAuthorised(menuNode)) {
                    children.add(menuNode);
                }
            } else {
                if (isLeafNodeAuthorised(menuNode)) {
                    MenuNode authorisedMenuChild = new MenuNode();
                    authorisedMenuChild.setId(menuNode.getId());
                    authorisedMenuChild.setName(menuNode.getName());
                    authorisedMenuChild.setParent(menuNode.getParent());
                    authorisedMenuChild.setTermSlug(menuNode.getTermSlug());
                    children.add(authorisedMenuChild);
                    createAuthorisedMenu(menuNode.getChildren(), authorisedMenuChild);
                }
            }
        }
        authorisedMenuRoot.setChildren(children);
    }

    private boolean isLeafNodeAuthorised(MenuNode leafNode) {
        String termSlug = leafNode.getTermSlug();
        int roleId = AUTH_CREDENTIALS.getRoleId();
        String userRoleId = Integer.toString(roleId);
        TermDTO termDTO = new TermDTO();
        termDTO.setAuthCredentials(AUTH_CREDENTIALS);
        CMSService cmss = new CMSService();
        termDTO.setTermSlug(termSlug);
        termDTO = cmss.getTermDetails(termDTO);
        if (termDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            return false;
        }
        Map<String, Object> term = termDTO.getTermDetails();
        List<String> termRoleIds = (List<String>) term.get(CMSConstants.TERM_ROLES);
        if (termRoleIds.contains(userRoleId)) {
            return true;
        }
        return false;
    }

    private void getLeafMenuList(List<MenuNode> leafMenuNodeList, MenuNode menuNode) {
        List<MenuNode> menuNodeChildren = menuNode.getChildren();
        if (menuNodeChildren == null) {
            leafMenuNodeList.add(menuNode);

        } else {
            for (MenuNode menuNodeChild : menuNodeChildren) {
                getLeafMenuList(leafMenuNodeList, menuNodeChild);
            }
        }
    }
}
