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

package org.labkey.ontologymanagement.job;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.pipeline.PipeRoot;
import org.labkey.api.pipeline.PipelineJob;
import org.labkey.api.util.URLHelper;
import org.labkey.api.security.User;
import org.labkey.api.view.ViewBackgroundInfo;
import org.labkey.api.pipeline.PipelineService;
import org.labkey.api.util.UnexpectedException;
import org.labkey.ontologymanagement.model.OntologyManagementManager;
import org.labkey.api.util.DateUtil;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class OntologyImportJob extends PipelineJob
{
    private Container _container ;
    private Integer _ontologyId;
    private User _user;

    @JsonCreator
    protected OntologyImportJob(
            @JsonProperty("_container") Container container,
            @JsonProperty("_ontologyId") Integer ontologyId,
            @JsonProperty("_user") User user)
    {
        super();
        _container = container;
        _ontologyId = ontologyId;
        _user = user;
    }


    public OntologyImportJob(ViewBackgroundInfo info, Container container, Integer ontologyId, User user, PipeRoot root)
    {
        super(null, info, root);
        _container = container;
        _ontologyId = ontologyId;
        _user = user;
        try
            {
                setLogFile(File.createTempFile("OntologyImportJob", ".tmp"));
            }
            catch (IOException e)
            {
                throw new UnexpectedException(e);
            }
        }

    @Override
    public URLHelper getStatusHref(){
        return null;
    }

    @Override
    public String getDescription(){
        return null;
    }

    @Override
    public void run(){
        setStatus("IMPORTING", "Job started at: " + DateUtil.nowISO());
        writeImportStatus("IMPORTING");

        getLogger().info("Loading Ontology: " + _ontologyId);
        try{
           OntologyManagementManager.getInstance().importOntology(_container, _ontologyId, _user); 
       }catch (Exception e)
            {
                error("Job failed: " + e.getMessage());
                writeImportStatus("ERROR");
                setStatus(TaskStatus.error);
                return;
            }
        getLogger().info("Done importing for Ontology " + _ontologyId);
        writeImportStatus("COMPLETE");
        setStatus(TaskStatus.complete);
    }

    private void writeImportStatus(String status){
        DbScope scope = DbScope.getLabKeyScope();
        try (DbScope.Transaction tx = scope.ensureTransaction()){
            SQLFragment update = new SQLFragment("UPDATE ontologymanagement.ontology SET importstatus = ? WHERE container = ? and rowid = ?;",status,_container,_ontologyId);
            new SqlExecutor(scope).execute(update);
            tx.commit();
        }
    }

}