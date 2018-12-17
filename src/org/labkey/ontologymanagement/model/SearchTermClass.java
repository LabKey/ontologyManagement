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

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */


public class SearchTermClass
{
    private String _term = "";
    private String ontologyid = "";
    private int limit = 40;

    public String getTerm()
    {
        return _term;
    }

    public void setTerm(String term)
    {
        _term = term;
    }

    public String getOntologyid()
    {
        return ontologyid;
    }

    public void setOntologyid(String ontologyid)
    {
        this.ontologyid = ontologyid;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }
}