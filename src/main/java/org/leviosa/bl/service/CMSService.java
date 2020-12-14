/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.leviosa.bl.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hedwig.cloud.response.HedwigResponseCode;
import org.hedwig.leviosa.constants.AWSMeta;
import org.hedwig.cms.dto.AwsS3DTO;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.MediaDTO;
import org.hedwig.cms.dto.TermDTO;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.hedwig.leviosa.constants.MediaMeta;
import org.hedwig.cms.dto.TermMetaDTO;
import org.leviosa.db.DAO.TermInstanceDAO;
import org.leviosa.db.DAO.TermMetaDAO;
import org.leviosa.db.entities.Term;
import org.leviosa.db.entities.TermInstance;
import org.leviosa.db.entities.TermInstancePK;
import org.leviosa.db.entities.TermInstanceRelations;
import org.leviosa.db.entities.TermMeta;
import org.leviosa.db.entities.TermRole;

/**
 *
 * @author bhaduri
 */
public class CMSService extends CMSServiceCore {

    /**
     * Retrieves all the meta keys for a particular term slug. Returns null in
     * case the term slug does not exists. Suppose a term slug has 4 fields. A
     * List of 5 Maps will be returned. 4 of them will be the fields along with
     * their different attributes like datatype, description, display priority
     * etc. The first will be the a default field called term instance slug.
     * This field is a mandatory field which will identify the instance when the
     * term instance is created. So for each term a term instance slug field is
     * mandatory. This field map contains the allow delete flag based upon
     * whether this term is defined as a parent of other term or not.
     *
     * @param termMetaDTO containing termSlug unique identifier for a particular
     * term (entity)
     * @return List of Map objects. Each map object is a meta key along with its
     * attributes. A default map object is always present for term instance slug
     * corresponding to the unique identifier for a term instance. null if
     * termSlug is absent.
     */
    public TermMetaDTO getTermMetaList(TermMetaDTO termMetaDTO) {
        String termSlug = termMetaDTO.getTermSlug();
        if (getTerm(termSlug) == null) {
            //term does not exist
            termMetaDTO.setResponseCode(HedwigResponseCode.TERM_NOT_EXISTS);
            return termMetaDTO;
        }
        List<Map<String, Object>> termMetaFields = new ArrayList<>();
        //field for term instance slug
        Map<String, Object> termInstanceSlugField = new HashMap<>();
        termInstanceSlugField.put(CMSConstants.TERM_SLUG, termSlug);
        termInstanceSlugField.put(CMSConstants.META_KEY, CMSConstants.TERM_INSTANCE_SLUG);
        termInstanceSlugField.put(CMSConstants.TERM_NAME, termSlug);//term name may not be required for this field
        termInstanceSlugField.put(CMSConstants.DATA_TYPE, "termslugfield");
        termInstanceSlugField.put(CMSConstants.TERM_META_DESCRIPTION, "Term Instance Slug");
        termInstanceSlugField.put(CMSConstants.DISPLAY_PRIORITY, -1);
        termInstanceSlugField.put(CMSConstants.FIELD_MANDATORY_FLAG, true);
        termInstanceSlugField.put(CMSConstants.FIELD_DETAILS, "An Unique itentifier for the data you are entering");
        termInstanceSlugField.put(CMSConstants.MANY_TO_ONE_TERM, null);
        termInstanceSlugField.put(CMSConstants.DATA_TYPE_DESC, "An Unique itentifier for the data you are entering");
        termInstanceSlugField.put(CMSConstants.MANY_TO_ONE_TERM_LIST, null);
        termInstanceSlugField.put(CMSConstants.DISABLE_ON_SCREEN_FLAG, false);
        termInstanceSlugField.put(CMSConstants.RENDER_ON_SCREEN_FLAG, true);
        termInstanceSlugField.put(CMSConstants.RENDER_ON_GRID_FLAG, false);
        List<Term> childTermList = getChildTermList(termSlug);
        if (!childTermList.isEmpty()) {
            termInstanceSlugField.put(CMSConstants.ALLOW_DELETE_FLAG, false);
        } else {
            termInstanceSlugField.put(CMSConstants.ALLOW_DELETE_FLAG, true);
        }

        termMetaFields.add(termInstanceSlugField);

        TermMetaDAO termMetaDAO = new TermMetaDAO(DatabaseConnection.EMF);
        List<TermMeta> termMetaOrderedList = termMetaDAO.getOrderedTermMetaList(termSlug);
        if (termMetaOrderedList == null) {
            //no meta data definition exists for term
            termMetaDTO.setResponseCode(HedwigResponseCode.TERM_META_NOT_EXISTS);
            return termMetaDTO;
        }

        for (TermMeta termMeta : termMetaOrderedList) {
            Map<String, Object> termScreenField = getTermMetaInMap(termMeta);
            if (!childTermList.isEmpty()) {
                termScreenField.put(CMSConstants.ALLOW_DELETE_FLAG, false);
            } else {
                termScreenField.put(CMSConstants.ALLOW_DELETE_FLAG, true);
            }
            termMetaFields.add(termScreenField);
        }
        termMetaDTO.setTermMetaFields(termMetaFields);
        Map<String, String> termMetaFieldLabels = termMetaFields
                .stream()
                .collect(Collectors.toMap(x -> (String) x.get(CMSConstants.META_KEY), x -> (String) x.get(CMSConstants.TERM_META_DESCRIPTION)));
        termMetaDTO.setTermMetaFieldLabels(termMetaFieldLabels);

        termMetaDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termMetaDTO;
    }

    /**
     * Gets a term instance for a particular termSlug and a the termInstanceSlug
     * in a map as a key value pair. The key of the map is the meta key of the
     * particular term. The value corresponding to each meta key is the actual
     * value for the for that meta key. In case of select many fields the value
     * is an array of termInstanceSlugs of the selected options. Corresponding
     * to each meta key there is another key with "Desc" appendend to it. The
     * value corresponding to the "Desc" field is same in all cases except for
     * select many and select one. For select one, it contains the value for the
     * term instance with highest display priority. For select many, it contains
     * the first selected element with the value of the term instance with
     * highest display priority.
     *
     * @param termInstanceDTO with termSlug and termInstanceSlug populated
     * @return Map of all the values for the meta keys of a particular term.
     */
    public TermInstanceDTO getTermInstance(TermInstanceDTO termInstanceDTO) {
        String termSlug = termInstanceDTO.getTermSlug();
        String termInstanceSlug = termInstanceDTO.getTermInstanceSlug();
        if (getTerm(termSlug) == null) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_NOT_EXISTS);
            return termInstanceDTO;
        }
        if (!isExistsTermInstanceSlug(termInstanceSlug)) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_INSTANCE_NOT_EXISTS);
            return termInstanceDTO;
        }
        TermMetaDTO termMetaDTO = new TermMetaDTO();
        termMetaDTO.setTermSlug(termSlug);
        List<Map<String, Object>> termMetaList = getTermMetaList(termMetaDTO).getTermMetaFields();
        Map<String, Object> termInstance = new HashMap<>();
        TermInstancePK termMetaDataPK;
        TermInstance termMetaData;
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        termInstance.put(CMSConstants.TERM_SLUG, termSlug);
        termInstance.put(CMSConstants.TERM_INSTANCE_SLUG, termInstanceSlug);
        boolean firstField = true;
        for (Map<String, Object> termMeta : termMetaList) {
            String termMetaKey = (String) termMeta.get(CMSConstants.META_KEY);
            termInstance.put(CMSConstants.META_KEY, termMetaKey);

            switch (termMetaKey) {

                case CMSConstants.TERM_INSTANCE_SLUG:
                    //termInstance.put(termMetaKey, termInstanceSlug);
                    //termInstance.put(termMetaKey + "Desc", getTermInstanceFirstField(termSlug, termInstanceSlug));
                    break;
                default:
                    String dataType = (String) termMeta.get(CMSConstants.DATA_TYPE);
                    switch (dataType) {
                        case CMSConstants.DATA_TYPE_SELECT_MANY:
                            String selectManyTermSlug = (String) termMeta.get(CMSConstants.MANY_TO_ONE_TERM);
                            String[] termInstancesSelectedRelations = getTermInstanceRelations(termSlug, termMetaKey, termInstanceSlug);
                            termInstance.put(termMetaKey, termInstancesSelectedRelations);

                            if (termInstancesSelectedRelations.length > 0) {
                                String selectManyTermInstanceSlugFirst = termInstancesSelectedRelations[0];
                                termInstance.put(termMetaKey + "Desc", getTermInstanceFirstField(selectManyTermSlug, selectManyTermInstanceSlugFirst) + "...");
                            }
                            break;
                        case CMSConstants.DATA_TYPE_SELECT_ONE:
                            termMetaDataPK = new TermInstancePK(termSlug, termMetaKey, termInstanceSlug);
                            termMetaData = termInstanceDAO.findTermInstance(termMetaDataPK);
                            if (termMetaData != null) {

                                String selectOneTermSlug = (String) termMeta.get(CMSConstants.MANY_TO_ONE_TERM);
                                String selectOneTermIncstanceSlug = termMetaData.getTermInstanceValue();
                                termInstance.put(termMetaKey, selectOneTermIncstanceSlug);
                                termInstance.put(termMetaKey + "Desc", getTermInstanceFirstField(selectOneTermSlug, selectOneTermIncstanceSlug));

                            } else {
                                termInstance.put(termMetaKey, null);
                                termInstance.put(termMetaKey + "Desc", null);
                            }
                            break;
                        default:
                            termMetaDataPK = new TermInstancePK(termSlug, termMetaKey, termInstanceSlug);
                            termMetaData = termInstanceDAO.findTermInstance(termMetaDataPK);
                            if (termMetaData != null) {
                                termInstance.put(termMetaKey, termMetaData.getTermInstanceValue());
                                termInstance.put(termMetaKey + "Desc", termMetaData.getTermInstanceValue());
                            } else {
                                termInstance.put(termMetaKey, null);
                                termInstance.put(termMetaKey + "Desc", null);

                            }
                            break;
                    }
                    if (firstField) {
                        termInstanceDTO.setTermInstanceFirstField((String)termInstance.get(termMetaKey+"Desc"));
                        firstField= false;
                    }
                    

            }

        }

        boolean isAttached = termInstanceIsAttached(termSlug, termInstanceSlug);

        if (isAttached) {
            termInstance.put(CMSConstants.ALLOW_DELETE_FLAG, false);
        } else {
            termInstance.put(CMSConstants.ALLOW_DELETE_FLAG, true);
        }
        termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        termInstanceDTO.setTermInstance(termInstance);
        return termInstanceDTO;
    }

    public TermInstanceDTO getChildTermInstanceList(TermInstanceDTO termInstanceDTO) {
        String parentTermSlug = termInstanceDTO.getParentTermSlug();
        String childTermMetaKey = termInstanceDTO.getChildTermMetaKey();
        String parentTermInstanceSlug = termInstanceDTO.getParentTermInstanceSlug();
        String childTermSlug = termInstanceDTO.getTermSlug();
        List<String> childTermInstanceSlugList = getChildTermInstanceList(parentTermSlug, parentTermInstanceSlug,childTermMetaKey, childTermSlug);
        List<Map<String, Object>> childTermInstanceList = new ArrayList<>();
        TermInstanceDTO childTermInstanceDTO = new TermInstanceDTO();
        for (String childTermInstanceSlug : childTermInstanceSlugList) {
            childTermInstanceDTO.setHedwigAuthCredentials(termInstanceDTO.getHedwigAuthCredentials());
            childTermInstanceDTO.setTermInstanceSlug(childTermInstanceSlug);
            childTermInstanceDTO.setTermSlug(childTermSlug);
            childTermInstanceDTO = getTermInstance(childTermInstanceDTO);
            if (childTermInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
                termInstanceDTO.setResponseCode(childTermInstanceDTO.getResponseCode());
            }
            Map<String, Object> childTermInstance = childTermInstanceDTO.getTermInstance();
            childTermInstanceList.add(childTermInstance);
        }
        termInstanceDTO.setTermInstanceList(childTermInstanceList);
        termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termInstanceDTO;
    }

    /**
     * Get the list of all term Instances for a particular term slug. Calls
     * getTermInstance.
     *
     * @param termInstanceDTO with termSlug populated
     * @return List of Map of term instances for a particular term..
     */
    public TermInstanceDTO getTermInstanceList(TermInstanceDTO termInstanceDTO) {
        String termSlug = termInstanceDTO.getTermSlug();
        TermInstanceDAO metaDataDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        List<String> termInstanceSlugList = metaDataDAO.getDistinctTermMetaDataSlugs(termSlug);
        if (termInstanceSlugList == null || termInstanceSlugList.isEmpty()) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_INSTANCE_NOT_EXISTS);
            return termInstanceDTO;
        }
        List<Map<String, Object>> termInstanceList = new ArrayList<>();
        for (String termInstanceSlug : termInstanceSlugList) {
            termInstanceDTO.setTermInstanceSlug(termInstanceSlug);
            termInstanceDTO = getTermInstance(termInstanceDTO);
            if (termInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
                return termInstanceDTO;
            }
            Map<String, Object> screenTermInstance = termInstanceDTO.getTermInstance();
            termInstanceList.add(screenTermInstance);
        }
        termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        termInstanceDTO.setTermInstanceList(termInstanceList);
        return termInstanceDTO;
    }

    public TermInstanceDTO deleteTermInstance(TermInstanceDTO termInstanceDTO) {
        String termSlug = termInstanceDTO.getTermSlug();
        String termInstanceSlug = termInstanceDTO.getTermInstanceSlug();
        int response;

        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        List<TermInstance> termInstanceList = termInstanceDAO.getTermInstance(termSlug, termInstanceSlug);
        if (termInstanceList.isEmpty()) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_INSTANCE_NOT_EXISTS);
            return termInstanceDTO;
        } else {
            response = termInstanceDAO.destroyTermInstance(termInstanceList);
            if (response != HedwigResponseCode.SUCCESS) {
                termInstanceDTO.setResponseCode(response);
                return termInstanceDTO;
            }
        }

        termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termInstanceDTO;
    }

    public TermInstanceDTO saveTermInstance(TermInstanceDTO termInstanceDTO) {
        TermInstanceDAO termInstanceDAO = new TermInstanceDAO(DatabaseConnection.EMF);
        Map<String, Object> termInstanceInMap = termInstanceDTO.getTermInstance();
        String termInstanceSlug = (String) termInstanceInMap.get(CMSConstants.TERM_INSTANCE_SLUG);
        String termSlug = (String) termInstanceInMap.get(CMSConstants.TERM_SLUG);
        //term does not exist..then return
        if (getTerm(termSlug) == null) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_NOT_EXISTS);
            return termInstanceDTO;
        }
        int response;
        //check if term instance exists. If exists this is update mode. So updateTermInstance = true
        boolean updateTermInstance = isExistsTermInstanceSlug(termInstanceSlug);
        //get the list fields for the term in termMetaList
        TermMetaDTO termMetaDTO = new TermMetaDTO();
        termMetaDTO.setTermSlug(termSlug);
        List<Map<String, Object>> termMetaList = getTermMetaList(termMetaDTO).getTermMetaFields();
        //loop through each field of the term and create a termInstance object for each meta
        //in case of select many a list of TermInstanceRelations object will be created and attached to the termInstance object
        //end of this loop will result in a List <TermInstance> which will be either updated or inserted
        //number of items in the termInstanceList will be equal to the number of fields(meta) for the term
        //create blank termInstanceList
        List<TermInstance> termInstanceList = new ArrayList<>();
        //start looping through each field of the term
        for (Map<String, Object> termMetaField : termMetaList) {
            //get the term meta key
            String termMetaKey = (String) termMetaField.get(CMSConstants.META_KEY);
            TermMeta termMeta = new TermMeta(termSlug, termMetaKey);
            switch (termMetaKey) {
                case CMSConstants.TERM_INSTANCE_SLUG:
                    break;
                default:
                    //creating a termInstance object for each meta
                    TermInstancePK termInstancePK = new TermInstancePK(termSlug, termMetaKey, termInstanceSlug);
                    TermInstance termInstance = new TermInstance(termInstancePK);
                    termInstance.setTermMeta(termMeta);
                    //in case of select many a list of TermInstanceRelations object is created
                    if (termMetaField.get(CMSConstants.DATA_TYPE).toString().equals(CMSConstants.DATA_TYPE_SELECT_MANY)) {
                        //in case of select many termInstanceValue is set to null

                        termInstance.setTermInstanceValue(null);

                        //get the slugs of the termInstances that are to be attached
                        ArrayList<String> selectManyTermInstancesList = (ArrayList<String>) termInstanceInMap.get(termMetaKey);
                        //Check related instance slug exists or not
                        response = checkRelatedSlugExistence(termSlug, termMetaKey, termInstanceSlug, selectManyTermInstancesList);
                        if (response != HedwigResponseCode.SUCCESS) {
                            termInstanceDTO.setResponseCode(HedwigResponseCode.DB_ILLEGAL_ORPHAN);
                            return termInstanceDTO;
                        }
                        //create term instance relation list
                        List<TermInstanceRelations> termInstanceRelationsList = new ArrayList<>();
                        for (String relatedTermInstanceSlug : selectManyTermInstancesList) {
                            TermInstanceRelations termInstanceRelations = new TermInstanceRelations(termSlug, termMetaKey, termInstanceSlug, relatedTermInstanceSlug);
                            termInstanceRelationsList.add(termInstanceRelations);
                        }
                        //add term instance relation list to term instance
                        termInstance.setTermInstanceRelationsList(termInstanceRelationsList);

                    } else {
                        //there are no term instance relations so the values will directly get stored
                        String termInstanceValue = (String) termInstanceInMap.get(termMetaKey);
                        //before assigning the value it should be checked whether this is a select one field
                        if (termMetaField.get(CMSConstants.DATA_TYPE).toString().equals(CMSConstants.DATA_TYPE_SELECT_ONE)) {
                            //check to see the related term instance going to be saved is existing or not
                            //Check related instance slug exists or not

                            response = checkRelatedSlugExistence(termSlug, termMetaKey, termInstanceSlug, termInstanceValue);
                            if (response != HedwigResponseCode.SUCCESS) {
                                termInstanceDTO.setResponseCode(HedwigResponseCode.DB_ILLEGAL_ORPHAN);
                                return termInstanceDTO;
                            }
                        }
                        termInstance.setTermInstanceValue(termInstanceValue);
                        //TermInstanceRelationsList is set to a blank array list
                        termInstance.setTermInstanceRelationsList(new ArrayList<>());
                    }
                    termInstanceList.add(termInstance);

            }

        }
        //check whether we have come here for update or create
        if (updateTermInstance) {

            response = termInstanceDAO.editTermInstance(termInstanceList);
            if (response != HedwigResponseCode.SUCCESS) {
                termInstanceDTO.setResponseCode(response);
                return termInstanceDTO;
            }
        } else {

            response = termInstanceDAO.createTermInstance(termInstanceList);
            if (response != HedwigResponseCode.SUCCESS) {
                termInstanceDTO.setResponseCode(response);
                return termInstanceDTO;
            }

        }
        termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termInstanceDTO;
    }

    public TermDTO getTermDetails(TermDTO termDTO) {
        String termSlug = termDTO.getTermSlug();
        Map<String, Object> termDetails = new HashMap<>();
        Term term = getTerm(termSlug);
        if (term == null) {
            termDTO.setResponseCode(HedwigResponseCode.TERM_NOT_EXISTS);
            return termDTO;
        }
        List<TermRole> termRoleList = term.getTermRoleList();
        List<String> termRoleIdList = new ArrayList<>();
        for (TermRole termRole : termRoleList) {
            String termRoleId = Integer.toString(termRole.getTermRolePK().getRoleId());
            termRoleIdList.add(termRoleId);
        }
        termDetails.put(CMSConstants.TERM_SLUG, term.getTermSlug());
        termDetails.put(CMSConstants.TERM_NAME, term.getName());
        termDetails.put(CMSConstants.TERM_DESCRIPTION, term.getDescription());
        termDetails.put(CMSConstants.TERM_SCREEN, term.getScreen());
        termDetails.put(CMSConstants.TERM_ROLES, termRoleIdList);
        termDTO.setTermDetails(termDetails);
        termDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termDTO;
    }

    public TermDTO getRootTermList(TermDTO termDTO) {
        List<Map<String, Object>> rootTermList = new ArrayList<>();
        List<Term> termList = getTermList();
        if (termList.isEmpty()) {
            termDTO.setResponseCode(HedwigResponseCode.DB_NON_EXISTING);
            return termDTO;
        }
        for (Term term : termList) {
            List<TermMeta> termMetaList = term.getTermMetaList();
            boolean isTermRoot = true;
            for (TermMeta termMeta : termMetaList) {
                if (termMeta.getManyToOneTermSlug() != null) {
                    isTermRoot = false;
                }
            }
            if (isTermRoot) {
                Map<String, Object> termDetails = new HashMap<>();
                List<TermRole> termRoleList = term.getTermRoleList();
                List<String> termRoleIdList = new ArrayList<>();
                for (TermRole termRole : termRoleList) {
                    String termRoleId = Integer.toString(termRole.getTermRolePK().getRoleId());
                    termRoleIdList.add(termRoleId);
                }
                termDetails.put(CMSConstants.TERM_SLUG, term.getTermSlug());
                termDetails.put(CMSConstants.TERM_NAME, term.getName());
                termDetails.put(CMSConstants.TERM_DESCRIPTION, term.getDescription());
                termDetails.put(CMSConstants.TERM_SCREEN, term.getScreen());
                termDetails.put(CMSConstants.TERM_ROLES, termRoleIdList);
                rootTermList.add(termDetails);
            }
        }
        if (termList.isEmpty()) {
            termDTO.setResponseCode(HedwigResponseCode.DB_NON_EXISTING);
            return termDTO;
        } else {
            termDTO.setResponseCode(HedwigResponseCode.SUCCESS);
            termDTO.setTermList(rootTermList);
            return termDTO;
        }
    }

    public TermDTO getChildTermList(TermDTO termDTO) {
        String parentTermSlug = termDTO.getTermSlug();
        List<Term> childTermList = getChildTermList(parentTermSlug);
        List<Map<String, Object>> childTermListInMap = new ArrayList<>();
        for (Term childTerm : childTermList) {
            Map<String, Object> childTermInMap = new HashMap<>();
            List<TermRole> termRoleList = childTerm.getTermRoleList();
            List<String> termRoleIdList = new ArrayList<>();
            for (TermRole termRole : termRoleList) {
                String termRoleId = Integer.toString(termRole.getTermRolePK().getRoleId());
                termRoleIdList.add(termRoleId);
            }
            childTermInMap.put(CMSConstants.TERM_SLUG, childTerm.getTermSlug());
            childTermInMap.put(CMSConstants.TERM_NAME, childTerm.getName());
            childTermInMap.put(CMSConstants.TERM_DESCRIPTION, childTerm.getDescription());
            childTermInMap.put(CMSConstants.TERM_SCREEN, childTerm.getScreen());
            childTermInMap.put(CMSConstants.TERM_ROLES, termRoleIdList);
            childTermListInMap.add(childTermInMap);
        }
        termDTO.setTermList(childTermListInMap);
        termDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return termDTO;
    }

    /**
     *
     * @param termMetaDTO with termSlug field populated.
     * @return
     */
    public TermMetaDTO getChildTermMetaList(TermMetaDTO termMetaDTO) {
        String parentTermSlug = termMetaDTO.getTermSlug();
        List<TermMeta> childTermMetaList = getChildTermMetaList(parentTermSlug);
        List<Map<String, Object>> childTermMetaListInMap = new ArrayList<>();
        for (TermMeta childTermMeta : childTermMetaList) {
            Map<String, Object> childTermMetaInMap = getTermMetaInMap(childTermMeta);
            childTermMetaInMap.put(CMSConstants.ALLOW_DELETE_FLAG, true);
            childTermMetaListInMap.add(childTermMetaInMap);
        }
        termMetaDTO.setTermMetaFields(childTermMetaListInMap);

        return termMetaDTO;
    }

    public TermInstanceDTO isExistsTermInstanceSlug(TermInstanceDTO termInstanceDTO) {
        String termInstanceSlug = termInstanceDTO.getTermInstanceSlug();
        if (isExistsTermInstanceSlug(termInstanceSlug)) {
            termInstanceDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        } else {
            termInstanceDTO.setResponseCode(HedwigResponseCode.TERM_INSTANCE_NOT_EXISTS);
        }
        return termInstanceDTO;
    }

    public TermInstanceDTO addMedia(TermInstanceDTO termInstanceDTO) {
        int mediaKeyId = generateMediaKey(termInstanceDTO);
        String mediaKey = String.format("%010d", mediaKeyId);
        String termInstanceSlug = convertToAlphabets(mediaKeyId);
        Map<String, Object> screenTermInstance = new HashMap<>();
        screenTermInstance.put(MediaMeta.MEDIA_USER, termInstanceDTO.getHedwigAuthCredentials().getUserId());
        screenTermInstance.put(MediaMeta.MEDIA_NAME, "Name");
        screenTermInstance.put(MediaMeta.MEDIA_KEY, mediaKey);
        screenTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, termInstanceSlug);
        screenTermInstance.put(CMSConstants.TERM_SLUG, "media");

        termInstanceDTO.setTermInstance(screenTermInstance);
        return termInstanceDTO;
    }

    public MediaDTO uploadMedia(MediaDTO mediaDTO) {

        String tempFilePath = mediaDTO.getMediaFilePath();
        
        Map<String, Object> mediaTermInstance = mediaDTO.getMediaTermInstance();
        int seriesId = Integer.parseInt((String) mediaTermInstance.get(MediaMeta.MEDIA_KEY));
        //Getting bucket name, secret key, access key from term instance settings
        TermInstanceDTO awsCredTermInstance = new TermInstanceDTO();
        awsCredTermInstance.setHedwigAuthCredentials(mediaDTO.getHedwigAuthCredentials());
        awsCredTermInstance.setTermSlug(CMSConstants.AWS_CRED_TERM_SLUG);
        awsCredTermInstance.setTermInstanceSlug("awsdefault");
        awsCredTermInstance = getTermInstance(awsCredTermInstance);
        Map<String, Object> awsCred = awsCredTermInstance.getTermInstance();
        String bucketName = (String) awsCred.get(AWSMeta.AWS_BUCKET_NAME);
        String accessKey = (String) awsCred.get(AWSMeta.AWS_ACCESS_KEY);
        String secretKey = (String) awsCred.get(AWSMeta.AWS_SECRET_KEY);
        
        //AWS S3 Bucket upload
        File tempFile = new File(tempFilePath);
        String userName = (String) mediaTermInstance.get(MediaMeta.MEDIA_USER);
        int tenantId = mediaDTO.getHedwigAuthCredentials().getTenantId();
        int productId = mediaDTO.getHedwigAuthCredentials().getProductId();

        String bucketKey = "product" + productId + "/tenant" + tenantId + "/" + userName + "/" + String.format("%010d", seriesId);
        AwsS3DTO awsS3DTO = new AwsS3DTO();
        awsS3DTO.setAWSBucketName(bucketName);
        awsS3DTO.setUploadFromLocalPath(tempFile.getParent());
        awsS3DTO.setUploadLocalFileName(tempFile.getName());
        awsS3DTO.setAWSKeyName(bucketKey);

        AwsS3Service awsS3Service = new AwsS3Service(accessKey, secretKey);
        awsS3DTO = awsS3Service.uploadToS3(awsS3DTO);

        if (awsS3DTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            Logger.getLogger(CMSServiceCore.class.getName()).log(Level.SEVERE, "Problem in aws upload");
            mediaDTO.setResponseCode(awsS3DTO.getResponseCode());
            return mediaDTO;
        }
        mediaTermInstance.put(MediaMeta.MEDIA_URL, awsS3DTO.getS3bucketUrl());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(mediaDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermInstance(mediaTermInstance);
        termInstanceDTO = saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            mediaDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return mediaDTO;
        }
        mediaDTO.setMediaTermInstance(mediaTermInstance);
        mediaDTO.setResponseCode(HedwigResponseCode.SUCCESS);
        return mediaDTO;
    }

    public MediaDTO deleteMedia(MediaDTO mediaDTO) {
        TermInstanceDTO awsCredTermInstance = new TermInstanceDTO();
        awsCredTermInstance.setHedwigAuthCredentials(mediaDTO.getHedwigAuthCredentials());
        awsCredTermInstance.setTermSlug(CMSConstants.AWS_CRED_TERM_SLUG);
        awsCredTermInstance.setTermInstanceSlug("awsdefault");
        awsCredTermInstance = getTermInstance(awsCredTermInstance);
        Map<String, Object> awsCred = awsCredTermInstance.getTermInstance();
        String bucketName = (String) awsCred.get(AWSMeta.AWS_BUCKET_NAME);
        String accessKey = (String) awsCred.get(AWSMeta.AWS_ACCESS_KEY);
        String secretKey = (String) awsCred.get(AWSMeta.AWS_SECRET_KEY);

        Map<String, Object> mediaTermInstance = mediaDTO.getMediaTermInstance();
        String userName = (String) mediaTermInstance.get(MediaMeta.MEDIA_USER);
        int productId = mediaDTO.getHedwigAuthCredentials().getProductId();
        int tenantId = mediaDTO.getHedwigAuthCredentials().getTenantId();
        String bucketKey = "product" + productId + "/tenant" + tenantId + "/" + userName + "/" + (String) mediaTermInstance.get(MediaMeta.MEDIA_KEY);
        
        AwsS3Service awsS3Service = new AwsS3Service(accessKey, secretKey);

        AwsS3DTO awsS3DTO = new AwsS3DTO();
        awsS3DTO.setAWSBucketName(bucketName);
        awsS3DTO.setAWSKeyName(bucketKey);

        awsS3DTO = awsS3Service.deleteFromS3(awsS3DTO);

        if (awsS3DTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            mediaDTO.setResponseCode(awsS3DTO.getResponseCode());
            return mediaDTO;
        }
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setTermInstance(mediaTermInstance);
        String mediaTermSlug = (String) mediaTermInstance.get(CMSConstants.TERM_SLUG);
        String mediaTermInstanceSlug = (String) mediaTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        termInstanceDTO.setTermSlug(mediaTermSlug);
        termInstanceDTO.setTermInstanceSlug(mediaTermInstanceSlug);
        termInstanceDTO = deleteTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != HedwigResponseCode.SUCCESS) {
            mediaDTO.setResponseCode(termInstanceDTO.getResponseCode());
        }
        return mediaDTO;
    }

    private int generateMediaKey(TermInstanceDTO termInstanceDTO) {

        termInstanceDTO.setTermSlug("media");
        termInstanceDTO = getTermInstanceList(termInstanceDTO);
        List<Map<String, Object>> mediaList = termInstanceDTO.getTermInstanceList();
        int maxId;
        try {
            if (mediaList.isEmpty()) {
                maxId = 1;
            } else {
                Map<String, Object> maxData = mediaList.stream().max(Comparator.comparingInt(ds -> Integer.parseInt((String) ds.get(MediaMeta.MEDIA_KEY)))).get();
                maxId = Integer.parseInt((String) maxData.get(MediaMeta.MEDIA_KEY));
                maxId = maxId + 1;
            }
        } catch (NullPointerException ne) {
            maxId = 1;
        }
        return maxId;
    }

    private String convertToAlphabets(int num) {
        HashMap<String, String> intToAlphMap = new HashMap<>();
        intToAlphMap.put("0", "a");
        intToAlphMap.put("1", "b");
        intToAlphMap.put("2", "c");
        intToAlphMap.put("3", "d");
        intToAlphMap.put("4", "e");
        intToAlphMap.put("5", "f");
        intToAlphMap.put("6", "g");
        intToAlphMap.put("7", "h");
        intToAlphMap.put("8", "i");
        intToAlphMap.put("9", "j");

        String alph = Integer.toString(num);

        String convertedInt = "";
        for (int i = 0; i < alph.length(); i++) {
            int startPos = i;
            int endPos = i + 1;
            String c = alph.substring(startPos, endPos);
            String cToA = intToAlphMap.get(c);
            convertedInt = convertedInt + cToA;
        }

        return convertedInt;
    }

}
