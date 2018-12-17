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

import org.labkey.api.data.Entity;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class OntologyMapping extends Entity
{
    private String _ontologyid = "";
    private String _concepturi = "";
    private String _fieldname = "";
    private String _schemaname = "";
    private String _queryname = "";
    private String _conceptlabel="";
    private Integer _rowId;

    public String getOntologyid()
    {
        return _ontologyid;
    }

    public void setOntologyid(String ontologyid)
    {
        _ontologyid = ontologyid;
    }
    public String getConcepturi()
    {
        return _concepturi;
    }

    public void setConcepturi(String concepturi)
    {
        _concepturi = concepturi;
    }


    public String getSchemaname()
    {
        return _schemaname;
    }

    public void setSchemaname(String schemaname)
    {
        _schemaname = schemaname;
    }
	
	    public String getQueryname()
    {
        return _queryname;
    }

    public void setQueryname(String queryname)
    {
        _queryname = queryname;
    }
	
	public String getFieldname()
    {
        return _fieldname;
    }

    public void setFieldname(String fieldname)
    {
        _fieldname = fieldname;
    }

    public Integer getRowId()
    {
        return _rowId;
    }

    public void setRowId(Integer rowId)
    {
        _rowId = rowId;
    }

    public String getConceptlabel()
    {
        return _conceptlabel;
    }

    public void setConceptlabel(String conceptlabel)
    {
        _conceptlabel = conceptlabel;
    }
}