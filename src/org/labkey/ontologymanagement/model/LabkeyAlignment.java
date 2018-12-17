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

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class LabkeyAlignment extends Entity
{
    private Integer _rowId;
    private String _ontologyrun = "";
    private String _ontologyprop ="";
    private String _labkeyproperty ="";

    /*Object to align Labkey with external ontology*/
    public LabkeyAlignment()
    {
    }

    public LabkeyAlignment(String ontologyrun, String ontologyprop, String labkeyproperty){
        _ontologyrun = StringUtils.trimToEmpty(ontologyrun);
        _ontologyprop = StringUtils.trimToEmpty(ontologyprop);
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

    public String getOntologyprop()
    {
        return _ontologyprop;
    }

    public void setOntologyprop(String ontologyprop)
    {
        _ontologyprop = ontologyprop;
    }

    public String getLabkeyproperty()
    {
        return _labkeyproperty;
    }

    public void setLabkeyproperty(String labkeyproperty)
    {
        _labkeyproperty = labkeyproperty;
    }

    public Integer getRowId()
    {
        return _rowId;
    }

    public void setRowId(Integer rowId)
    {
        _rowId = rowId;
    }
}
