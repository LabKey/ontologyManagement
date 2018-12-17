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

package org.labkey.ontologymanagement.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.Filter;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableResultSet;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.ontologymanagement.OntologyManagementSchema;
import org.springframework.validation.Errors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import com.fasterxml.jackson.core.type.TypeReference;


import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */


public class OntologyManagementManager
{
    private static final OntologyManagementManager _instance = new OntologyManagementManager();
    private static final String DEFAULT_ALIGNMENT_JSON = "web/defaultLabkeyAlignment.json";

    private OntologyManagementManager()
    {
        // prevent external construction with a private default constructor
    }

    public static OntologyManagementManager getInstance()
    {
        return _instance;
    }

    public void deleteAllData(Container c) throws SQLException
    {
        // delete all ontologies / Alignements / Ontology Data and Annotations when the container is deleted:
        Filter containerFilter = SimpleFilter.createContainerFilter(c);
        Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntology(), containerFilter);
        Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntologyMapping(), containerFilter);
        Table.delete(OntologyManagementSchema.getInstance().getTableInfoLabkeyAlignment(), containerFilter);
        Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntologyData(), containerFilter);
    }

    public Ontology[] getOntologies(Container c)
    {
        Filter containerFilter = SimpleFilter.createContainerFilter(c);
        return getOntologies(containerFilter);
    }

    public void deleteOntology(Container c, int rowId) throws SQLException
    {
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction())
        {
            SimpleFilter onto_filter = SimpleFilter.createContainerFilter(c);
            onto_filter.addCondition(FieldKey.fromParts("RowId"), rowId);
            Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntology(), onto_filter);

            //Delete from ontology-data table
            SimpleFilter data_filter = SimpleFilter.createContainerFilter(c);
            data_filter.addCondition(FieldKey.fromParts("ontologyrun"), String.valueOf(rowId));
            Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntologyData(), data_filter);

            //Delete from labkey alignment table
            SimpleFilter align_filter = SimpleFilter.createContainerFilter(c);
            align_filter.addCondition(FieldKey.fromParts("ontologyrun"), String.valueOf(rowId));
            Table.delete(OntologyManagementSchema.getInstance().getTableInfoLabkeyAlignment(), align_filter);

            tx.commit();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public OntologyMapping[] getOntologyAnnotations(Container c, String schemaName, String queryName) throws SQLException
    {
        SimpleFilter filter = SimpleFilter.createContainerFilter(c);
        filter.addCondition(FieldKey.fromParts("schemaname"), schemaName);
        filter.addCondition(FieldKey.fromParts("queryname"), queryName);
        return new TableSelector(OntologyManagementSchema.getInstance().getTableInfoOntologyMapping(), filter, new Sort("RowId")).getArray(OntologyMapping.class);
    }

    public void setDefaultOntologyLabkeyAlignment(Container c, int rowId,User user) throws SQLException
    {
        Map<String, String> map = null;
        try
        {
            //Get the exploded directories path
            ClassLoader webappClassLoader = getClass().getClassLoader();
            Method m = webappClassLoader.getClass().getMethod("getExplodedModuleDirectories");
            List<File> explodedModuleDirs = (List<File>)m.invoke(webappClassLoader);

            // Try to find the Alignment file inside of each one of the exploded directories (Method based on the ModuleLoader.java for module.xml)
            for(File moduleDir : explodedModuleDirs)
            {
                File defAlignmentJson = new File(moduleDir, DEFAULT_ALIGNMENT_JSON);
                try
                {
                    if (defAlignmentJson.exists())
                    {
                        map = new ObjectMapper().readValue(
                                defAlignmentJson,
                                new TypeReference<Map<String, String>>()
                                {
                                });
                    }
                } catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction())
        {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                LabkeyAlignment lkAlignment= new LabkeyAlignment(Integer.toString(rowId),key,value);
                lkAlignment.setContainer(c.getId());
                Table.insert(user, OntologyManagementSchema.getInstance().getTableInfoLabkeyAlignment(),lkAlignment );
            }
            tx.commit();

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void importOntology(Container c, int rowId, User user) throws SQLException
    {
        Ontology onto = getOntology(c, rowId);
        //Delete the data from the previous run
        SimpleFilter filter = SimpleFilter.createContainerFilter(c);
        filter.addCondition(FieldKey.fromParts("ontologyrun"), Integer.toString(rowId));
        Table.delete(OntologyManagementSchema.getInstance().getTableInfoOntologyData(), filter);

        //Import New Data
        QueryOntology queryOntology = new QueryOntology(onto.getEndpoint(),onto.getQuery());
        QueryExecution qexec = queryOntology.executeQuery();

        // Get the ontology labkey alignment
        DbScope scope = DbScope.getLabKeyScope();
        SQLFragment select = new SQLFragment("SELECT * FROM ontologymanagement.labkeyalign WHERE container = ? and ontologyrun = ?", c, Integer.toString(rowId));
        TableResultSet r = new SqlSelector(scope, select).getResultSet();

        HashMap<String, String> lkAlignment = new HashMap<String, String>();

        // Each result row
        for (Map<String, Object> row : r)
        {
            lkAlignment.put(row.get("ontologyprop").toString(),row.get("labkeyproperty").toString());
        }

        try {
            Iterator<QuerySolution> results = qexec.execSelect();
            for ( ; results.hasNext() ; ){
                QuerySolution qs = results.next();
                Iterator<String> varNames = qs.varNames();
                RDFNode subject = qs.get(queryOntology.getSubjectName());
                RDFNode property = qs.get(queryOntology.getPropertyName());
                RDFNode object = qs.get(queryOntology.getObjectName());
                if(lkAlignment.get(property.toString())!= null)
                {
                    OntologyData ontologyData = new OntologyData(onto.getRowId().toString(), onto.getOntologyid(), subject.toString(), property.toString(), object.toString(), lkAlignment.get(property.toString()));
                    ontologyData.setContainer(c.getId());
                    Table.insert(user, OntologyManagementSchema.getInstance().getTableInfoOntologyData(), ontologyData);
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            qexec.close();
        }

    }

    public Ontology getOntology(Container c, int rowId)
    {
        SimpleFilter filter = SimpleFilter.createContainerFilter(c);
        filter.addCondition(FieldKey.fromParts("RowId"), rowId);
        Ontology[] people = getOntologies(filter);
        if (people != null && people.length > 0)
            return people[0];
        else
            return null;
    }

    private Ontology[] getOntologies(Filter filter)
    {
        return new TableSelector(OntologyManagementSchema.getInstance().getTableInfoOntology(), filter, new Sort("RowId")).getArray(Ontology.class);
    }

    public static Ontology[] getOntologyRuns(Container c, String ontologyid)

    {
        SimpleFilter filter = SimpleFilter.createContainerFilter(c);
         filter.addCondition(OntologyManagementSchema.getInstance().getTableInfoOntology().getColumn("ontologyid"),ontologyid);

        return new TableSelector(OntologyManagementSchema.getInstance().getTableInfoOntology(), filter, new Sort("RowId")).getArray(Ontology.class);
    }

    public Ontology insertOntology(Container c, User user, Ontology ontology) throws SQLException
    {
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction())
        {
            ontology.setContainer(c.getId());
            ontology = Table.insert(user, OntologyManagementSchema.getInstance().getTableInfoOntology(), ontology);
            setDefaultOntologyLabkeyAlignment(c,ontology.getRowId(),user);
            tx.commit();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return ontology;
    }

    public Ontology updateOntology(Container c, User user, Ontology ontology, Object ts) throws SQLException
    {
        if (ontology.getRowId() == null)
            throw new IllegalStateException("Can't update a row with a null rowId");
        if (ontology.getContainerId() == null)
            ontology.setContainerId(c.getId());
        if (!ontology.getContainerId().equals(c.getId()))
            throw new IllegalStateException("Can't update a row with a null rowId");
        if( user == null )
            throw new IllegalStateException("User Null");
        if( c == null )
            throw new IllegalStateException("Container is Null");
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction())
        {
            ontology =  Table.update(user, OntologyManagementSchema.getInstance().getTableInfoOntology(), ontology, ontology.getRowId());
            tx.commit();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return ontology;
    }


    public static void validate(Ontology ontology, Errors errors)
    {
        if (StringUtils.trimToNull(ontology.getEndpoint()) == null)
            errors.rejectValue("endpoint", null, null, "Endpoint is required.");
        if (StringUtils.trimToNull(ontology.getOntologyname()) == null)
            errors.rejectValue("ontologyname", null, null, "Ontology Name is required.");
        if (StringUtils.trimToNull(ontology.getOntologyid()) == null)
            errors.rejectValue("ontologyid", null, null, "Table Name to store the ontology is required.");
        if (StringUtils.trimToNull(ontology.getQuery()) == null)
        {
            errors.rejectValue("query", null, null, "Query to extract the data from the ontology is required.");
        }else{
            if (!ontology.getQuery().contains("?subject")|| !ontology.getQuery().contains("?property") || !ontology.getQuery().contains("?object"))
            {
                errors.rejectValue("query", null, null, "The Query output must be ?subject ?property ?object.");
            }
        }
    }
}