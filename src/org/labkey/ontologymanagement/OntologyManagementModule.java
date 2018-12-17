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

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.util.PageFlowUtil;

import org.labkey.api.view.WebPartFactory;
import org.labkey.ontologymanagement.model.OntologyManagementManager;
import org.labkey.ontologymanagement.model.Ontology;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class OntologyManagementModule extends DefaultModule
{
    public String getName()
    {
        return "OntologyManagement";
    }

    public double getVersion()
    {
        return 18.12;
    }

    protected void init()
    {
        addController("ontologymanagement", OntologyManagementController.class);
    }

    @Override
    protected @NotNull Collection<? extends WebPartFactory> createWebPartFactories()
    {
        return Collections.emptyList();
    }

    public boolean hasScripts()
    {
        return true;
    }

    @Override
    protected void doStartup(ModuleContext moduleContext)
    {
        ContainerManager.addContainerListener(new OntologyManagementContainerListener());
        registerSchemas();
    }

    @NotNull
    public Collection<String> getSummary(Container c)
    {
        Ontology[] ontologyList = OntologyManagementManager.getInstance().getOntologies(c);
        if (ontologyList != null && ontologyList.length > 0)
        {
            Collection<String> list = new LinkedList<>();
            list.add("OntologyManagement Module: " + ontologyList.length + " ontology records.");
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return PageFlowUtil.set(OntologyManagementSchema.getInstance().getSchemaName());
    }

    protected void registerSchemas()
    {
        for (final String schemaName : getSchemaNames())
        {
            DbSchema dbSchema = DbSchema.get(schemaName);
            DefaultSchema.registerProvider(dbSchema.getQuerySchemaName(), new DefaultSchema.SchemaProvider(this)
            {
                public QuerySchema createSchema(final DefaultSchema schema, Module module)
                {
                    DbSchema dbSchema = DbSchema.get(schemaName);
                    return QueryService.get().createSimpleUserSchema(dbSchema.getQuerySchemaName(), null, schema.getUser(), schema.getContainer(), dbSchema);
                }
            });
        }
    }
}