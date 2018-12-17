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


public class OntologyData extends Entity
{
    private String _ontologyrun;
    private String _subject;
    private Integer _rowId;
    private String _ontologyid;
    private String _object;
    private String _property;
    private String _labkeyproperty;

    public OntologyData()
    {
    }

    public OntologyData(String ontologyrun, String ontologyid, String subject, String property, String object, String labkeyproperty)
    {
        _ontologyrun = StringUtils.trimToEmpty(ontologyrun);
        _subject = StringUtils.trimToEmpty(subject);
        _ontologyid = StringUtils.trimToEmpty(ontologyid);
        _object = object;
        _property = StringUtils.trimToEmpty(property);
        _labkeyproperty = StringUtils.trimToEmpty(labkeyproperty);
    }

    public String getOntologyrun()
    {
        return _ontologyrun;
    }

    public void setOntologyrun(String ontologyrun)
    {
        _ontologyrun = ontologyrun;
    }

    public String getSubject()
    {
        return _subject;
    }

    public void setSubject(String subject)
    {
        _subject = subject;
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

        return Objects.equals(_ontologyrun, p.getOntologyname()) &&
                Objects.equals(_subject, p.getEndpoint()) &&
                Objects.equals(_ontologyid, p.getOntologyid()) &&
                Objects.equals(_object, p.getOntologyid()) &&
                Objects.equals(_property, p.getQuery());
    }

    public String getOntologyid()
    {
        return _ontologyid;
    }

    public void setOntologyid(String ontologyid)
    {
        _ontologyid = ontologyid;
    }

    public String getObject()
    {
        return _object;
    }

    public void setObject(String object)
    {
        _object = object;
    }

    public String getProperty()
    {
        return _property;
    }

    public void setProperty(String property)
    {
        _property = property;
    }

    public String getLabkeyproperty()
    {
        return _labkeyproperty;
    }

    public void setLabkeyproperty(String labkeyproperty)
    {
        _labkeyproperty = labkeyproperty;
    }
}