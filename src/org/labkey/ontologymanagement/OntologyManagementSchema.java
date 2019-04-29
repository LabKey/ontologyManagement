/*
 * Copyright (c) 2018 Nestec Ltd. 
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

import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

 public class OntologyManagementSchema
{
    private static final OntologyManagementSchema _instance = new OntologyManagementSchema();
    private static final String SCHEMA_NAME = "ontologymanagement";

    public static OntologyManagementSchema getInstance()
    {
        return _instance;
    }

    private OntologyManagementSchema()
    {
    }

    public String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    public DbSchema getSchema()
    {
        return DbSchema.get(SCHEMA_NAME, DbSchemaType.Module);
    }

    public SqlDialect getSqlDialect()
    {
        return getSchema().getSqlDialect();
    }

    public TableInfo getTableInfoOntology()
    {
        return getSchema().getTable("ontology");
    }
    public TableInfo getTableInfoOntologyData()
    {
        return getSchema().getTable("ontologydata");
    }
    public TableInfo getTableInfoLabkeyAlignment()
    {
        return getSchema().getTable("labkeyalign");
    }
    public TableInfo getTableInfoOntologyMapping()
    {
        return getSchema().getTable("ontologymapping");
    }
}
