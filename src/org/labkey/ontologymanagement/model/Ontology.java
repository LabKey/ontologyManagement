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
import org.labkey.api.data.Entity;
import java.util.Objects;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */


public class Ontology extends Entity
{
    private String _ontologyname;
    private String _endpoint;
    private String _query;
    private Integer _rowId;
    private String _ontologyid;

    public Ontology()
    {
    }

    public Ontology(String ontologyname, String endpoint, String query,String ontologyid)
    {
        _ontologyname = StringUtils.trimToEmpty(ontologyname);
        _endpoint = StringUtils.trimToEmpty(endpoint);
        _query = query;
        _ontologyid = ontologyid;
    }

    public String getQuery()
    {
        return _query;
    }

    public void setQuery(String query)
    {
        _query = query;
    }

    public String getOntologyname()
    {
        return _ontologyname;
    }

    public void setOntologyname(String ontologyname)
    {
        _ontologyname = ontologyname;
    }

    public String getEndpoint()
    {
        return _endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        _endpoint = endpoint;
    }

    public Integer getRowId()
    {
        return _rowId;
    }

    public void setRowId(Integer rowId)
    {
        _rowId = rowId;
    }


    public boolean equals(Object obj)
    {
        if (!(obj instanceof Ontology))
            return false;
        Ontology p = (Ontology)obj;

        return Objects.equals(_ontologyname, p.getOntologyname()) &&
                Objects.equals(_endpoint, p.getEndpoint()) &&
                Objects.equals(_ontologyid, p.getOntologyid()) &&
                Objects.equals(_query, p.getQuery());
    }

    public String getOntologyid()
    {
        return _ontologyid;
    }

    public void setOntologyid(String ontologyid)
    {
        _ontologyid = ontologyid;
    }
}
