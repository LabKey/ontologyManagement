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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class QueryOntology
{
    public QueryOntology(String endpoint, String query){
        _endpoint = StringUtils.trimToEmpty(endpoint);
        _query = StringUtils.trimToEmpty(query);
    }
    private String _endpoint;

    private String _query;
    private String _subjectName = "subject";
    private String _propertyName = "property";
    private String _objectName = "object";

    public void setEndpoint(String endpoint)
    {
        _endpoint = endpoint;
    }

    public String getEndpoint()
    {
        return _endpoint;
    }

    public String getQuery()
    {
        return _query;
    }

    public void setQuery(String query)
    {
        _query = query;
    }


    public QueryExecution executeQuery(){
        QueryExecution qexec = null;

        // Check if the endpoint is a local file or a SPARQL endpoint
        if (urlValidator(_endpoint))
        {
            //  creating query object
            Query query = QueryFactory.create(_query);
            // initializing queryExecution factory with remote service.
            qexec = QueryExecutionFactory.sparqlService(_endpoint, _query);
        }else
        {
            // Try to load as local file
            Model model = ModelFactory.createDefaultModel() ;
            model.read(_endpoint) ;
            Query query = QueryFactory.create(_query);
            qexec = QueryExecutionFactory.create(query, model);
        }
        return qexec;
    }

    public String getSubjectName()
    {
        return _subjectName;
    }

    public void setSubjectName(String subjectName)
    {
        _subjectName = subjectName;
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyName = propertyName;
    }

    public String getObjectName()
    {
        return _objectName;
    }

    public void setObjectName(String objectName)
    {
        _objectName = objectName;
    }

    public static boolean urlValidator(String url)
    {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (URISyntaxException exception) {
            return false;
        }
        catch (MalformedURLException exception) {
            return false;
        }
    }

}