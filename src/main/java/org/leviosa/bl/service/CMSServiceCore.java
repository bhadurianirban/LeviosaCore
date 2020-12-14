package org.leviosa.bl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;

import org.hedwig.cloud.response.HedwigResponseCode;
import org.hedwig.leviosa.constants.CMSConstants;
import org.leviosa.db.DAO.TermDAO;
import org.leviosa.db.DAO.TermInstanceRelationsDAO;
import org.leviosa.db.DAO.TermMetaDAO;
import org.leviosa.db.DAO.TermInstanceDAO;
import org.leviosa.db.JPA.exceptions.NonexistentEntityException;
import org.leviosa.db.JPA.exceptions.PreexistingEntityException;
import org.leviosa.db.entities.Term;
import org.leviosa.db.entities.TermInstance;
import org.leviosa.db.entities.TermInstanceRelations;
import org.leviosa.db.entities.TermMeta;
import org.leviosa.db.entities.TermMetaPK;

/**
 *
 * @author dgrf-iv
 */
public class CMSServiceCore {

    public List<Term> getTermList() {
        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);
        List<Term> termList = termDAO.findTermEntities();

        return termList;
    }

    public int insertIntoTerm(Term term) {
        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);

        int response = termDAO.createTerm(term);
        return response;

    }

    protected boolean isExistsTermInstanceSlug(String termInstanceSlug) {
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        List<TermInstance> termInstanceList = termInstanceDAO.getTermInstanceByInstanceSluug(termInstanceSlug);
        return !termInstanceList.isEmpty();
    }

    public int insertIntoTermMeta(TermMeta termMeta) {
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);
        Term term = termDAO.findTerm(termMeta.getTermMetaPK().getTermSlug());
        int displayPriority = this.maxDisplayPriority(termMeta.getTermMetaPK().getTermSlug());
        termMeta.setDisplayPriority(displayPriority);
        termMeta.setTerm(term);
        try {
            termMetaDAO.create(termMeta);
            return HedwigResponseCode.SUCCESS;
        } catch (PreexistingEntityException e) {
            return HedwigResponseCode.DB_DUPLICATE;
        } catch (Exception ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }

    }

    public int editTerm(Term term) {

        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);

        int response = termDAO.editTerm(term);
        return response;

    }

    public int editTermMeta(TermMeta termMeta) {

        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);

        try {
            termMetaDAO.edit(termMeta);
            return HedwigResponseCode.SUCCESS;
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_NON_EXISTING;
        } catch (Exception ex) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, null, ex);
            return HedwigResponseCode.DB_SEVERE;
        }

    }

    public int deleteTerm(String termSlug) {

        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);
        Term term = new Term(termSlug);
        int response = termDAO.destroyTerm(term);
        return response;

    }

    public int deleteTermMetaAndData(String termSlug, String metaKey) {
        int response;
        TermMeta termMeta = new TermMeta(termSlug, metaKey);
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);

        response = termMetaDAO.destroyTermMeta(termMeta);
        if (response != HedwigResponseCode.SUCCESS) {
            return response;
        }
        return HedwigResponseCode.SUCCESS;
    }

    public Term getTerm(String termSlug) {
        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);
        Term term = termDAO.findTerm(termSlug);
        return term;
    }

//    public boolean isTermExists(String termSlug) {
//        TermDAO termDAO = new TermDAO(DatabaseConnection.EMF);
//        Term term = termDAO.findTerm(termSlug);
//        return term != null;
//    }
    public TermMeta getTermMeta(String termSlug, String metaKey) {
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);

        TermMetaPK metaPK = new TermMetaPK(termSlug, metaKey);
        TermMeta termMeta = termMetaDAO.findTermMeta(metaPK);

        return termMeta;
    }

    protected Map<String, Object> getTermMetaInMap(TermMeta termMeta) {
        Map<String, Object> termMetaInMap = new HashMap<>();
        termMetaInMap.put(CMSConstants.TERM_SLUG, termMeta.getTermMetaPK().getTermSlug());
        termMetaInMap.put(CMSConstants.TERM_NAME, termMeta.getTerm().getName());
        termMetaInMap.put(CMSConstants.META_KEY, termMeta.getTermMetaPK().getMetaKey());
        termMetaInMap.put(CMSConstants.DATA_TYPE, termMeta.getDataType());
        termMetaInMap.put(CMSConstants.TERM_META_DESCRIPTION, termMeta.getDescription());
        termMetaInMap.put(CMSConstants.DISPLAY_PRIORITY, termMeta.getDisplayPriority());
        termMetaInMap.put(CMSConstants.FIELD_MANDATORY_FLAG, termMeta.getMandatory());
        termMetaInMap.put(CMSConstants.FIELD_DETAILS, termMeta.getDetails());
        termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM, termMeta.getManyToOneTermSlug());
        termMetaInMap.put(CMSConstants.DATA_TYPE_DESC, getDataTypeLabel(termMeta.getDataType()));
        if (termMeta.getDataType().equals(CMSConstants.DATA_TYPE_SELECT_ONE) || termMeta.getDataType().equals(CMSConstants.DATA_TYPE_SELECT_MANY)) {
            Map<String, String> selectTermInstanceList = getTermInstancesFirstField(termMeta.getManyToOneTermSlug());
            termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM_LIST, selectTermInstanceList);
            termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM_DESC, getTerm(termMeta.getManyToOneTermSlug()).getName());
        } else {
            termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM_LIST, null);
            termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM_DESC, null);
        }
        //termMetaInMap.put(CMSConstants.MANY_TO_ONE_TERM_DESC, getTerm(termMeta.getManyToOneTermSlug()).getName());
        termMetaInMap.put(CMSConstants.DISABLE_ON_SCREEN_FLAG, false);
        termMetaInMap.put(CMSConstants.RENDER_ON_SCREEN_FLAG, true);
        termMetaInMap.put(CMSConstants.RENDER_ON_GRID_FLAG, true);

        return termMetaInMap;
    }

    public List<Map<String, Object>> getTermListInMap() {

        List<Term> termList = getTermList();
        List<Map<String, Object>> termListInMap = termList.stream().map(term -> {
            Map<String, Object> termInMap = new HashMap<>();
            termInMap.put(CMSConstants.TERM_SLUG, term.getTermSlug());
            termInMap.put(CMSConstants.TERM_NAME, term.getName());
            termInMap.put(CMSConstants.TERM_DESCRIPTION, term.getDescription());
            termInMap.put(CMSConstants.TERM_SCREEN, term.getScreen());
            //Check if term meta exists. If meta exists then do not allow deletion of the term.
            if (term.getTermMetaList().isEmpty()) {
                termInMap.put(CMSConstants.ALLOW_DELETE_FLAG, false);
            } else {
                termInMap.put(CMSConstants.ALLOW_DELETE_FLAG, true);
            }
            return termInMap;
        }).collect(Collectors.toList());
        return termListInMap;
    }

    private int maxDisplayPriority(String termSlug) {
        TermMetaDAO metaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        int maxPriority;
        maxPriority = metaDAO.getMaxDisplayPriority(termSlug);
        if (maxPriority == -1) {
            return 0;
        } else {
            int displayPriority = maxPriority + 1;
            return displayPriority;
        }

    }

    private Map<String, String> getTermInstancesFirstField(String termSlug) {
        //get list of term instances for a termSlug
        TermInstanceDAO termMetaDataDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        List<String> termInstanceSlugList = termMetaDataDAO.getDistinctTermMetaDataSlugs(termSlug);
        //List<LinkedHashMap<String, String>> termInstanceList = getTermInstancesForTerm(termSlug);
        Map<String, String> termInstancesFirstFieldList = new HashMap<>();
        termInstanceSlugList.forEach((termInstanceSlug) -> {
            String termInstanceFirstField = getTermInstanceFirstField(termSlug, termInstanceSlug);
            termInstancesFirstFieldList.put(termInstanceSlug, termInstanceFirstField);
        });

        return termInstancesFirstFieldList;
    }

    /**
     * Returns the data for the highest display priority for a particular term
     * instance of a term. Each term has multiple fields which are defined in
     * the term meta. The term meta stores the order in which the fields are
     * retrieved. This function reads the order of the term meta as defined and
     * then returns the data of all the fields of the instance in that order.
     *
     * @param termSlug
     * @param termInstanceSlug
     * @return
     */
    public String getTermInstanceFirstField(String termSlug, String termInstanceSlug) {

        //LinkedHashMap<String, String> termInstance = getTermInstanceForInstanceSlug(termSlug, termInstanceSlug);
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        List<TermInstance> termInstanceFieldOrderedList = termInstanceDAO.getTermInstance(termSlug, termInstanceSlug);
        return termInstanceFieldOrderedList.get(0).getTermInstanceValue();
    }

//    public String getTermName(String termSlug) {
//        if (termSlug == null) {
//            return null;
//        }
//        if (termSlug.equals("")) {
//            return null;
//        }
//        Term term = getTerm(termSlug);
//        return term.getName();
//    }
    //Get the list of terms which are attached to this termSlug through select many fields.
    public List<Term> getSelectManyAttachedTerms(String termSlug) {
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        List<TermMeta> attachedTermMetaKeys = termMetaDAO.getTermMetaListForSelectOneTerm(termSlug, "selectmany");
        List<Term> selectManyTermList = attachedTermMetaKeys.stream().map(tm -> tm.getTerm()).collect(Collectors.toList());

        return selectManyTermList;
    }

    //Get the list of terms which are attached to this termSlug through select one fields.
    public List<Term> getSelectOneAttachedTerms(String termSlug) {
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        List<TermMeta> attachedTermMetaKeys = termMetaDAO.getTermMetaListForSelectOneTerm(termSlug, "selectone");
        List<Term> selectOneTermList = attachedTermMetaKeys.stream().map(tm -> tm.getTerm()).collect(Collectors.toList());

        return selectOneTermList;
    }

    protected List<Term> getChildTermList(String termSlug) {
        List<Term> selectOneTermList = getSelectOneAttachedTerms(termSlug);
        List<Term> selectManyTermList = getSelectManyAttachedTerms(termSlug);
        List<Term> childTermList = Stream.concat(selectOneTermList.stream(), selectManyTermList.stream()).collect(Collectors.toList());
        return childTermList;
    }

    protected List<TermMeta> getChildTermMetaList(String parentTermSlug) {
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        List<TermMeta> childTermMetaSelectOne = termMetaDAO.getTermMetaListForSelectOneTerm(parentTermSlug, CMSConstants.DATA_TYPE_SELECT_ONE);
        List<TermMeta> childTermMetaSelectMany = termMetaDAO.getTermMetaListForSelectOneTerm(parentTermSlug, CMSConstants.DATA_TYPE_SELECT_MANY);
        List<TermMeta> childTermMetaList = Stream.concat(childTermMetaSelectOne.stream(), childTermMetaSelectMany.stream()).collect(Collectors.toList());
        return childTermMetaList;
    }

    protected List<String> getChildTermInstanceList(String parentTermSlug, String parentTermInstanceSlug, String childTermMetaKey, String childTermSlug) {
        List<String> childTermInstanceSlugList = new ArrayList<>();
        //get child term
        //get the data type of the child meta key
        TermMeta termMeta = getTermMeta(childTermSlug, childTermMetaKey);
        String childTermMetaDataType = termMeta.getDataType();

        if (childTermMetaDataType.equals(CMSConstants.DATA_TYPE_SELECT_MANY)) {
            //if it is select many query term instance relations table with the parentTermInstanceSlug matching 
            //the term_instance_relSlug and meta key as child meta key and term slug as child term slug
            //a list of unique term instance slugs should be obtained which will be returned
            TermInstanceRelationsDAO termInstanceRelationsDAO = new TermInstanceRelationsDAO(DatabaseConnection.EMF);
            List<TermInstanceRelations> termInstanceRelationsList = termInstanceRelationsDAO.getTermInstanceChildren(parentTermInstanceSlug, childTermSlug, childTermMetaKey);

            if (!termInstanceRelationsList.isEmpty()) {
                for (TermInstanceRelations childTermInstance : termInstanceRelationsList) {
                    childTermInstanceSlugList.add(childTermInstance.getTermInstanceRelationsPK().getTermInstanceSlug());
                }
            }

        } else if (childTermMetaDataType.equals(CMSConstants.DATA_TYPE_SELECT_ONE)) {
            TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
            List<TermInstance> childTermInstanceList = termInstanceDAO.getTermInstanceChildren(parentTermInstanceSlug, childTermSlug, childTermMetaKey);
            if (!childTermInstanceList.isEmpty()) {
                for (TermInstance childTermInstance : childTermInstanceList) {
                    childTermInstanceSlugList.add(childTermInstance.getTermInstancePK().getTermInstanceSlug());
                }
            }
        }
        //}

        //if it is select one query term instance  table with the parentTermInstanceSlug matching 
        //the term_instance_value and meta key as child meta key and term slug as child term slug
        //a list of unique term instance slugs should be obtained which will be returned
        return childTermInstanceSlugList;
    }

    public boolean termInstanceIsAttached(String attachedTermSlug, String attachedTermInstanceSlug) {
        //check whether attached in select many relations
        TermInstanceRelationsDAO termInstanceRelationsDAO = new TermInstanceRelationsDAO(DatabaseConnection.EMF);
        List<TermInstanceRelations> termInstanceRelationsList = termInstanceRelationsDAO.getTermInstanceAttachments(attachedTermInstanceSlug);
        if (termInstanceRelationsList.isEmpty()) {
            //get list of terms and meta keys where the attachedTermSlug is defined as a select one field
            TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
            List<TermMeta> attachedTermMetaKeys = termMetaDAO.getTermMetaListForSelectOneTerm(attachedTermSlug, "selectone");

            if (!attachedTermMetaKeys.isEmpty()) {
                //get the list of term instance fields where the meta term is attached
                TermInstanceDAO termMetaDataDAO = new TermInstanceDAO(DatabaseConnection.EMF);
                for (TermMeta termMeta : attachedTermMetaKeys) {
                    List<TermInstance> termMetaDataList = termMetaDataDAO.getMetaDataBySlugAndMetakey(termMeta.getTermMetaPK().getTermSlug(), termMeta.getTermMetaPK().getMetaKey());

                    if (!termMetaDataList.isEmpty()) {
                        //list is not empty means that there are instances where term is attached
                        for (TermInstance termMetaData : termMetaDataList) {
                            //for each meta key check whethe the attached value is null
                            if (termMetaData.getTermInstanceValue() != null) {
                                //if not null check whether it is equal to the termInstance
                                if (termMetaData.getTermInstanceValue().equals(attachedTermInstanceSlug)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }

    public int reorderTermMeta(List<Map<String, Object>> termScreenFields) {
        int response;
        for (int i = 0; i < termScreenFields.size(); i++) {
            Map<String, Object> termMetaInMap = termScreenFields.get(i);
            TermMeta termMeta = getTermMeta((String) termMetaInMap.get(CMSConstants.TERM_SLUG), (String) termMetaInMap.get("metaKey"));
            termMeta.setDisplayPriority(i);
            response = editTermMeta(termMeta);
            if (response != HedwigResponseCode.SUCCESS) {
                return response;
            }
        }

        return HedwigResponseCode.SUCCESS;
    }

    public LinkedHashMap<String, String> getTermMetaDataTypes() {
        LinkedHashMap<String, String> metaDataTypes = new LinkedHashMap<>();
        metaDataTypes.put("textfield", "Text Field");
        metaDataTypes.put("number", "Number");
        metaDataTypes.put("date", "Date");
        metaDataTypes.put("time", "Time");
        metaDataTypes.put("textarea", "Text Area");
        metaDataTypes.put("boolean", "Boolean Button");
        metaDataTypes.put("selectone", "Drop Down");
        metaDataTypes.put("selectmany", "Checkbox");
        return metaDataTypes;
    }

    public String getDataTypeLabel(String dataTypeValue) {
        LinkedHashMap<String, String> metaDataTypes = getTermMetaDataTypes();
        return metaDataTypes.get(dataTypeValue);
    }

    public String[] getTermInstanceRelations(String termSlug, String metaKey, String termInstanceSlug) {
        TermInstanceRelationsDAO termInstanceRelationsDAO = new TermInstanceRelationsDAO(DatabaseConnection.EMF);
        List<TermInstanceRelations> termInstanceRelationsList = termInstanceRelationsDAO.getTermInstanceRelations(termSlug, metaKey, termInstanceSlug);
        String[] termInstanceRelations = new String[termInstanceRelationsList.size()];
        for (int i = 0; i < termInstanceRelationsList.size(); i++) {
            termInstanceRelations[i] = termInstanceRelationsList.get(i).getTermInstanceRelationsPK().getTermInstanceRelSlug();
        }
        return termInstanceRelations;
    }

    protected int checkRelatedSlugExistence(String termSlug, String termMetaKey, String termInstanceSlug, List<String> termInstanceSlugRelations) {
        TermInstanceRelationsDAO termInstanceRelationsDAO = new TermInstanceRelationsDAO(DatabaseConnection.EMF);
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);

        for (String relatedTermInstanceSlug : termInstanceSlugRelations) {
            //Check related instance slug exists or not
            TermMetaPK termMetaPK = new TermMetaPK(termSlug, termMetaKey);
            TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
            TermMeta termMeta = termMetaDAO.findTermMeta(termMetaPK);
            String relatedTermSlug = termMeta.getManyToOneTermSlug();
            List<TermInstance> relatedTermInstanceValues = termInstanceDAO.getTermInstance(relatedTermSlug, relatedTermInstanceSlug);
            if (relatedTermInstanceValues == null || relatedTermInstanceValues.isEmpty()) {
                return HedwigResponseCode.DB_ILLEGAL_ORPHAN;
            }
        }
        return HedwigResponseCode.SUCCESS;
    }

    protected int checkRelatedSlugExistence(String termSlug, String termMetaKey, String termInstanceSlug, String relatedTermInstanceSlug) {
        TermInstanceRelationsDAO termInstanceRelationsDAO = new TermInstanceRelationsDAO(DatabaseConnection.EMF);
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);

        //Check related instance slug exists or not
        TermMetaPK termMetaPK = new TermMetaPK(termSlug, termMetaKey);
        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        TermMeta termMeta = termMetaDAO.findTermMeta(termMetaPK);
        String relatedTermSlug = termMeta.getManyToOneTermSlug();
        List<TermInstance> relatedTermInstanceValues = termInstanceDAO.getTermInstance(relatedTermSlug, relatedTermInstanceSlug);
        if (relatedTermInstanceValues == null || relatedTermInstanceValues.isEmpty()) {
            return HedwigResponseCode.DB_ILLEGAL_ORPHAN;
        }

        return HedwigResponseCode.SUCCESS;
    }
}
