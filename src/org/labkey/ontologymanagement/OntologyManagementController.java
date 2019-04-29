/*
 *Copyright (c) 2018 Nestec Ltd. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.ontologymanagement;

import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.MutatingApiAction;
import org.labkey.api.action.ReadOnlyApiAction;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.BeanViewForm;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DataRegion;
import org.labkey.api.data.DataRegionSelection;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableResultSet;
import org.labkey.api.exceptions.OptimisticConflictException;
import org.labkey.api.pipeline.PipelineService;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryAction;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.DeletePermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.GridView;
import org.labkey.api.view.HttpView;
import org.labkey.api.view.InsertView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.NotFoundException;
import org.labkey.api.view.RedirectException;
import org.labkey.api.view.UpdateView;
import org.labkey.api.view.ViewContext;
import org.labkey.api.view.template.PageConfig;
import org.labkey.ontologymanagement.job.OntologyImportJob;
import org.labkey.ontologymanagement.model.Ontology;
import org.labkey.ontologymanagement.model.OntologyData;
import org.labkey.ontologymanagement.model.OntologyManagementManager;
import org.labkey.ontologymanagement.model.OntologyMapping;
import org.labkey.ontologymanagement.model.SearchTermClass;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

@Marshal(Marshaller.Jackson)
public class OntologyManagementController extends SpringActionController
{
    private final static DefaultActionResolver _actionResolver = new DefaultActionResolver(OntologyManagementController.class);

    // SQL QUERIES
    private final static String INSERT_ONTO_MAPPING = "";


    public OntologyManagementController()
    {
        setActionResolver(_actionResolver);
    }

    public PageConfig defaultPageConfig()
    {
        return new PageConfig();
    }

    /*******************
    * API Actions
    ********************/

    // Insert the dataset annotation (mapping between the dataset and the ontology)
    @RequiresPermission(InsertPermission.class)
    public class InsertOntoMapping extends MutatingApiAction<OntologyMapping>
    {
        @Override
        public Object execute(OntologyMapping form, BindException errors)
        {
            DbScope scope = DbScope.getLabKeyScope();
            form.setContainer(getContainer().getId());
            try (DbScope.Transaction tx = scope.ensureTransaction())
            {
                form = Table.insert(getUser(), OntologyManagementSchema.getInstance().getTableInfoOntologyMapping(), form);
                tx.commit();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return form;
        }
    }

    // Insert a new concept for a given folder
    @RequiresPermission(InsertPermission.class)
    public class InsertNewConcept extends MutatingApiAction<OntologyData>
    {
        @Override
        public Object execute(OntologyData form, BindException errors)
        {

            DbScope scope = DbScope.getLabKeyScope();
            Ontology onto = new Ontology();
            form.setContainer(getContainer().getId());
            try (DbScope.Transaction tx = scope.ensureTransaction())
            {
                Ontology[] ontologyRuns = OntologyManagementManager.getOntologyRuns(getContainer(), form.getOntologyid());
                // Add Ontology if not exist
                if (ontologyRuns.length>0)
                {
                    //set the RowId as the lowest one
                    form.setOntologyrun(ontologyRuns[0].getRowId().toString());
                }else{
                    // Create ontology in container
                    onto.setContainer(getContainer().getId());
                    onto.setOntologyid(form.getOntologyid());
                    onto.setOntologyname(form.getOntologyid());
                    onto = Table.insert(getUser(),OntologyManagementSchema.getInstance().getTableInfoOntology(),onto);
                    form.setOntologyrun(onto.getRowId().toString());
                }

                // Add Ontology Data Entry
                form = Table.insert(getUser(), OntologyManagementSchema.getInstance().getTableInfoOntologyData(), form);

                //Update the Subject to include the line id for link purposes
                form.setSubject(form.getSubject()+form.getRowId());
                form = Table.update(getUser(), OntologyManagementSchema.getInstance().getTableInfoOntologyData(), form,form.getRowId());

                tx.commit();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return form;
        }
    }



    // Get all dataset annotations
    @RequiresPermission(ReadPermission.class)
    public class getOntologyAnnotations extends MutatingApiAction<OntologyMapping>
    {
        @Override
        public Object execute(OntologyMapping form, BindException errors) throws Exception
        {
            return OntologyManagementManager.getInstance().getOntologyAnnotations(getContainer(), form.getSchemaname(), form.getQueryname());
        }
    }

    // Get the dataset annotations for a specific column (mapping bettwen the dataset and the ontology)
    @RequiresPermission(ReadPermission.class)
    public class getOntoMapping extends ReadOnlyApiAction<OntologyMapping>
    {
        @Override
        public Object execute(OntologyMapping form, BindException errors)
        {
            DbScope scope = DbScope.getLabKeyScope();
            List<Map> ret = new ArrayList<>();
            try (DbScope.Transaction tx = scope.ensureTransaction()){
                SQLFragment select = new SQLFragment("SELECT * FROM ontologymanagement.ontologymapping WHERE container = ? and schemaname = ? and queryname = ? and fieldname = ?",
                        getContainer(),form.getSchemaname(),form.getQueryname(),form.getFieldname());
                TableResultSet r = new SqlSelector(scope, select).getResultSet();
                // Each result row
                for (Map<String, Object> row : r)
                {
                    row.put("conceptDescription", conceptInfoAux(scope,row.get("concepturi").toString()));
                    ret.add(row);
                }
                tx.commit();
            }catch(Exception e){
                e.printStackTrace();
            }
            return ret;
        }
    }

    // Delete dataset annotation (mapping bettwen the dataset and the ontology)

    @RequiresPermission(DeletePermission.class)
    public class DeleteOntoMapping extends MutatingApiAction<OntologyMapping>
    {
        @Override
        public Object execute(OntologyMapping form, BindException errors)
        {
            DbScope scope = DbScope.getLabKeyScope();
            form.setContainer(getContainer().getId());
            try (DbScope.Transaction tx = scope.ensureTransaction())
            {
                SimpleFilter filter = SimpleFilter.createContainerFilter(getContainer());
                filter.addCondition(FieldKey.fromParts("RowId"), form.getRowId());
                Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntologyMapping(), filter);
                tx.commit();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return form;
        }
    }

    // Get Available ontologies for a specific container (To be used in the future)

    @RequiresPermission(ReadPermission.class)
    public class GetAvailableOntologies extends ReadOnlyApiAction<Object>
    {
        @Override
        public Object execute(Object o, BindException errors)
        {
            GridView gridView = new GridView(getDataRegion(), errors);
            ApiSimpleResponse response = new ApiSimpleResponse();
            DbScope scope = DbScope.getLabKeyScope();

            SQLFragment select = new SQLFragment("SELECT * FROM ontologymanagement.ontology WHERE container in (?,?,?);",getContainer(),ContainerManager.getSharedContainer(), getContainer().getParent());

            TableResultSet r = new SqlSelector(scope, select).getResultSet();
            List<String> ret = new ArrayList<String>();
            for (Map<String, Object> row : r)
            {
                ret.add(row.get("ontologyname").toString());
            }

            return ret;
        }
    }

    // Get ontology concept information

    @RequiresPermission(ReadPermission.class)
    public class getConceptInfo extends ReadOnlyApiAction<OntologyData>
    {
        @Override
        public Object execute(OntologyData form, BindException errors)
        {
            DbScope scope = DbScope.getLabKeyScope();

            return conceptInfoAux(scope,form.getSubject());
        }
    }

    // Search for a specific term in the ontologies from the current container, parent container and Shared container.
    @RequiresPermission(ReadPermission.class)
    public class SearchTerm extends ReadOnlyApiAction<SearchTermClass>
    {
        @Override
        public Object execute(SearchTermClass form, BindException errors)
        {

            DbScope scope = DbScope.getLabKeyScope();

            /*Searching in the Study, Project and Shared folders*/
            SQLFragment select = new SQLFragment("SELECT distinct  subject, ontologyid, object, ts_rank_cd(weighted_tsv, query,8) AS rank\n" +
                    "            FROM ontologymanagement.ontologydata, to_tsquery(?) query\n" +
                    "            WHERE query @@ weighted_tsv ",form.getTerm().trim().replace(" ","&"));

            SQLFragment select_part2 = new SQLFragment(" and container in (?,?,?)\n" +
                    "            ORDER BY rank DESC\n" +
                    "            LIMIT ?;",
                    getContainer(),ContainerManager.getSharedContainer(), getContainer().getParent(),form.getLimit());

            //If is specified an ontology just search in that ontology
            if (!form.getOntologyid().isEmpty()){
                select.append( new SQLFragment("and ontologyid = ?",form.getOntologyid()));
            }
            // Append second part of the query
            select.append(select_part2);

            TableResultSet r = new SqlSelector(scope, select).getResultSet();

            // For each subject keep the label of the highest match
            Map<String, Map<String, Object>> ret  = new HashMap<>();
            for (Map<String, Object> row : r)
            {
                String row_subject = row.get("subject").toString();
                if(row.containsKey(row_subject))
                {
                    if (Float.parseFloat(ret.get(row_subject).get("rank").toString()) < Float.parseFloat(row.get("rank").toString()))
                    {
                        //Update label
                        ret.get(row_subject).put("object", row.get("object"));
                    }
                }else{
                    // Include result and add description
                    row.put("conceptDescription", conceptInfoAux(scope,row_subject));
                    ret.put(row_subject,row);
                }
            }
            return new ArrayList<>(ret.values());
        }
    }

    /*******************
     * Form View Actions
     ********************/

    // List Ontologies
    @RequiresPermission(AdminPermission.class)
    public class ListOntologies extends SimpleViewAction
    {
        @Override
        public ModelAndView getView(Object o, BindException errors)
        {
            GridView gridView = new GridView(getDataRegion(), errors);
            gridView.setSort(new Sort("Endpoint"));
            return gridView;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild("OntologyManagement", getURL());
        }

        public ActionURL getURL()
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }
    }

    // Insert New Ontology
    @RequiresPermission(AdminPermission.class)
    public class InsertAction extends FormViewAction<Ontology>
    {
        @SuppressWarnings("UnusedDeclaration")
        public InsertAction()
        {
        }

        public InsertAction(ViewContext ctx)
        {
            setViewContext(ctx);
        }

        @Override
        public void validateCommand(Ontology target, Errors errors)
        {
            OntologyManagementManager.validate(target, errors);
        }

        @Override
        public ModelAndView getView(Ontology ontology, boolean reshow, BindException errors) throws Exception
        {
            InsertView insertView = new InsertView(getDataRegion(), errors);
            insertView.setInitialValue("query","SELECT DISTINCT ?subject ?property ?object WHERE {?subject ?property ?object}");
            insertView.setInitialValue("importstatus", "Not Imported");
            return insertView;
        }

        @Override
        public boolean handlePost(Ontology ontology, BindException errors) throws Exception
        {
            try
            {
                OntologyManagementManager.getInstance().insertOntology(getContainer(), getUser(), ontology);
                return true;
            }
            catch (SQLException x)
            {
                errors.addError(new ObjectError("main", null, null, "Insert failed: " + x.getMessage()));
                return false;
            }
        }

        @Override
        public URLHelper getSuccessURL(Ontology ontology)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild("OntologyManagement", new ActionURL(ListOntologies.class, getContainer())).addChild("Insert Ontology");
        }

        public ActionURL getURL()
        {
            return new ActionURL(InsertAction.class, getContainer());
        }

    }


    // Update Ontology by clicking on the row id
    @RequiresPermission(AdminPermission.class)
    public class UpdateAction extends FormViewAction<OntologyForm>
    {
        private Ontology _ontology = null;
        
        public boolean handlePost(OntologyForm form, BindException errors) throws Exception
        {
            // Pass in timestamp for optimistic concurrency
            Object ts = null; // ((Map)form.getOldValues()).get("_ts");

            try
            {
                Ontology ontology = form.getBean();
                OntologyManagementManager.getInstance().updateOntology(getContainer(), getUser(), ontology, ts);
                return true;
            }
            catch (OptimisticConflictException x)
            {
                errors.addError(new ObjectError("main", new String[]{"Error"}, new Object[]{x}, x.getMessage()));
                if (x.getSQLException().getErrorCode() == Table.ERROR_ROWVERSION)
                    setReshow(false);
                return false;
            }
        }

        public HttpView getView(OntologyForm form, boolean reshow, BindException errors) throws Exception
        {
            // handles case where handlePost wants to force reselect
            if (!reshow)
                form.forceReselect();

            // get Ontology to generate nav trail later
            if (!form.isDataLoaded())
                form.refreshFromDb();
            _ontology = form.getBean();

            return new UpdateView(form, errors);
        }

        public ActionURL getSuccessURL(OntologyForm ontologyForm)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public void validateCommand(OntologyForm ontologyForm, Errors errors)
        {
            OntologyManagementManager.validate(ontologyForm.getBean(), errors);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild("OntologyManagement", new ActionURL(ListOntologies.class, getContainer())).addChild(getPageTitle());
        }

        public String getPageTitle()
        {
            String name = "";
            if (_ontology != null)
                name = " -- " + _ontology.getEndpoint() + ", " + _ontology.getOntologyname();
            return "Update Ontology" + name;
        }

        public ActionURL getURL(Ontology p)
        {
            return new ActionURL(UpdateAction.class, getContainer()).addParameter("rowId", ""+p.getRowId());
        }
    }

    // Delete ontology, its imported data and the alignments. The Annotations performed with this ontology are not delete.
    @RequiresPermission(AdminPermission.class)
    public class DeleteAction extends FormViewAction
    {
        public HttpView getView(Object o, boolean reshow, BindException errors) throws Exception
        {
            throw new RedirectException(getSuccessURL(o));
        }

        public boolean handlePost(Object o, BindException errors) throws Exception
        {
            Set<Integer> ontologyIds = DataRegionSelection.getSelectedIntegers(getViewContext(), true);
            for (Integer ontologyId : ontologyIds){
                OntologyManagementManager.getInstance().deleteOntology(getContainer(), ontologyId);
            }
            return true;
        }

        public void validateCommand(Object o, Errors errors)
        {
        }

        public ActionURL getSuccessURL(Object o)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }
    }

    // Edit Ontology by selecting and click in the edit button (Uses UpdateAction)
    @RequiresPermission(UpdatePermission.class)
    public class EditAction extends FormViewAction
    {
        public HttpView getView(Object o, boolean reshow, BindException errors) throws Exception
        {
            ActionURL updateURL = new ActionURL(UpdateAction.class, getContainer());

            throw new RedirectException(updateURL.toString()+"rowId="+DataRegionSelection.getSelectedIntegers(getViewContext(), true).iterator().next());

        }

        public boolean handlePost(Object o, BindException errors) throws Exception
        {

            return true;
        }

        public void validateCommand(Object o, Errors errors)
        {

        }
        public ActionURL getSuccessURL(Object o)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }
    }

    // Action to redirect to the querybrowser to edit the ontology alignments (requires to be an admin?)
    @RequiresPermission(AdminPermission.class)
    public class EditLabkeyAlignment extends FormViewAction
    {
        public HttpView getView(Object o, boolean reshow, BindException errors) throws Exception
        {
            QuerySchema querySchema = DefaultSchema.get(getUser(), getContainer(), OntologyManagementSchema.getInstance().getSchemaName());
            if (!(querySchema instanceof UserSchema))
                throw new NotFoundException("Could not find the schema '" + OntologyManagementSchema.getInstance().getSchemaName() + "' in the folder '" + getContainer().getPath() + "'!");
            UserSchema schema = (UserSchema)querySchema;
            ActionURL ontoDataURL = schema.urlFor(QueryAction.executeQuery);

            throw new RedirectException(ontoDataURL.toString() + "&query.queryName=labkeyalign&query.ontologyrun~eq="+DataRegionSelection.getSelectedIntegers(getViewContext(), true).iterator().next());

        }

        public boolean handlePost(Object o, BindException errors) throws Exception
        {

            return true;
        }

        public void validateCommand(Object o, Errors errors)
        {

        }
        public ActionURL getSuccessURL(Object o)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }
    }

    // Triggers the pipeline to import the ontology data regarding the alignements and the sparql query
    @RequiresPermission(AdminPermission.class)
    public class ImportOntologyAction extends FormViewAction
    {
        public HttpView getView(Object o, boolean reshow, BindException errors) throws Exception
        {
            throw new RedirectException(getSuccessURL(o));
        }

        public boolean handlePost(Object o, BindException errors) throws Exception
        {
            Set<Integer> ontologyIds = DataRegionSelection.getSelectedIntegers(getViewContext(), true);
            for (Integer ontologyId : ontologyIds){
                OntologyImportJob job = new OntologyImportJob(getViewBackgroundInfo(),getContainer(), ontologyId, getUser(),PipelineService.get().findPipelineRoot(getContainer()));
                PipelineService.get().queueJob(job);
                writeImportStatus("QUEUED", ontologyId);

            }
            return true;
        }

        public void validateCommand(Object o, Errors errors)
        {
        }

        public ActionURL getSuccessURL(Object o)
        {
            return new ActionURL(ListOntologies.class, getContainer());
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }
    }



    private DataRegion getDataRegion()
    {
        DataRegion rgn = new DataRegion();
        TableInfo tableInfo = OntologyManagementSchema.getInstance().getTableInfoOntology();
        rgn.setColumns(tableInfo.getColumns("RowId,Ontologyname, Endpoint, Query, Ontologyid, Importstatus"));

        DisplayColumn col = rgn.getDisplayColumn("RowId");
        ActionURL updateURL = new ActionURL(UpdateAction.class, getContainer());
        col.setURL(updateURL.toString() + "rowId=${RowId}");
        col.setDisplayPermission(UpdatePermission.class);
        DisplayColumn onto = rgn.getDisplayColumn("Ontologyid");

        QuerySchema querySchema = DefaultSchema.get(getUser(), getContainer(), OntologyManagementSchema.getInstance().getSchemaName());
        if (!(querySchema instanceof UserSchema))
            throw new NotFoundException("Could not find the schema '" + OntologyManagementSchema.getInstance().getSchemaName() + "' in the folder '" + getContainer().getPath() + "'!");
        UserSchema schema = (UserSchema)querySchema;
        ActionURL ontoDataURL = schema.urlFor(QueryAction.executeQuery);
        onto.setURL(ontoDataURL.toString() + "&query.queryName=ontologydata&query.ontologyid~eq=${Ontologyid}");
        onto.setDisplayPermission(ReadPermission.class);

        ButtonBar gridButtonBar = new ButtonBar();
        rgn.setShowRecordSelectors(true);

        ActionButton delete = new ActionButton(DeleteAction.class, "Delete");
        delete.setActionType(ActionButton.Action.POST);
        delete.setDisplayPermission(DeletePermission.class);
        delete.setRequiresSelection(true, "Are you sure you want to delete this ontology?", "Are you sure you want to delete these ontology?");
        gridButtonBar.add(delete);

        ActionButton edit = new ActionButton(EditAction.class,"Edit");
        edit.setActionType(ActionButton.Action.GET);
        edit.setDisplayPermission(UpdatePermission.class);
        edit.setRequiresSelection(true,1,1);
        gridButtonBar.add(edit);

        ActionButton editLabkeyAlign = new ActionButton(EditLabkeyAlignment.class,"Edit Alignment");
        editLabkeyAlign.setActionType(ActionButton.Action.GET);
        editLabkeyAlign.setDisplayPermission(UpdatePermission.class);
        editLabkeyAlign.setRequiresSelection(true,1,1);
        gridButtonBar.add(editLabkeyAlign);

        ActionButton insert = new ActionButton("Add Ontology", new InsertAction(getViewContext()).getURL());
        insert.setDisplayPermission(InsertPermission.class);
        gridButtonBar.add(insert);


        ActionButton importOntology = new ActionButton( ImportOntologyAction.class, "Import Ontology");
        importOntology.setActionType(ActionButton.Action.POST);
        importOntology.setDisplayPermission(InsertPermission.class);
        importOntology.setRequiresSelection(true, "This action will replace previous import runs.", "This action will replace previous import runs.");
        gridButtonBar.add(importOntology);

        rgn.setButtonBar(gridButtonBar, DataRegion.MODE_GRID);
        return rgn;
    }

    private DataRegion getOntologyDataRegion()
    {
        DataRegion rgn = new DataRegion();
        TableInfo tableInfo = OntologyManagementSchema.getInstance().getTableInfoOntologyData();
        rgn.setColumns(tableInfo.getColumns("RowId,ontologyrun,ontologyid,subject, property, object,labkeyproperty"));

        return rgn;
    }

    public static class OntologyForm extends BeanViewForm<Ontology>
    {
        public OntologyForm()
        {
            super(Ontology.class, OntologyManagementSchema.getInstance().getTableInfoOntology());
        }
    }

    public static class OntologyDataForm extends BeanViewForm<Ontology>
    {
        public OntologyDataForm()
        {
            super(Ontology.class, OntologyManagementSchema.getInstance().getTableInfoOntologyData());
        }
    }

    private void writeImportStatus(String status, Integer rowid){
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction()){
            SQLFragment update = new SQLFragment("UPDATE ontologymanagement.ontology SET importstatus = ? WHERE container = ? and rowid = ?;",status,getContainer(),rowid);
            new SqlExecutor(scope).execute(update);
            tx.commit();
        }
    }


    /*******************
     * Auxiliary Functions
     ********************/

    private List<Map> conceptInfoAux(DbScope scope, String subject){
        SQLFragment select = new SQLFragment("SELECT labkeyproperty,property,object FROM ontologymanagement.OntologyData WHERE subject= ? and container in (?,?,?) ",
                subject,getContainer(),ContainerManager.getSharedContainer(), getContainer().getParent());
        TableResultSet r = new SqlSelector(scope, select).getResultSet();
        List<Map> ret = new ArrayList<>();
        // Each result row
        for (Map<String, Object> row : r)
        {
            ret.add(row);
        }
        return ret;
    }
}