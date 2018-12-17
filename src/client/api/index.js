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

 /**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

let containerPath = LABKEY.ActionURL.getContainer() 
let baseUrl = LABKEY.ActionURL.getBaseURL()

export function getCookieValue(a) {
    var b = document.cookie.match('(^|;)\\s*' + a + '\\s*=\\s*([^;]+)');
    return b ? b.pop() : '';
}

export const API = {

    searchTerm : function(term){
        return fetch(`${baseUrl}${containerPath}/ontologymanagement-SearchTerm.api`, {
               credentials: "same-origin",
               method: 'POST',
               body: JSON.stringify({"term": term,"limit":40}),
               headers: {
                'content-type': 'application/json',
                'X-LABKEY-CSRF' : getCookieValue('X-LABKEY-CSRF')
             }
        });
    },
    postInsertOntoMapping : function(form){
        return fetch(`${baseUrl}${containerPath}/ontologymanagement-InsertOntoMapping.api`, {
                     credentials: "same-origin",
                     method: 'POST',
                     body: JSON.stringify(form),
                     headers: {
                        'content-type': 'application/json',
                        'X-LABKEY-CSRF' : getCookieValue('X-LABKEY-CSRF')
                     }
        });
    },
    postInsertNewConcept : function(form){
        return fetch(`${baseUrl}${containerPath}/ontologymanagement-InsertNewConcept.api`, {
                     credentials: "same-origin",
                     method: 'POST',
                     body: JSON.stringify(form),
                     headers: {
                        'content-type': 'application/json',
                        'X-LABKEY-CSRF' : getCookieValue('X-LABKEY-CSRF')
                     }
        });
    },
    postDeleteOntoMapping : function(form){
        return fetch(`${baseUrl}${containerPath}/ontologymanagement-DeleteOntoMapping.api`, {
                     credentials: "same-origin",
                     method: 'POST',
                     body: JSON.stringify(form),
                     headers: {
                        'content-type': 'application/json',
                        'X-LABKEY-CSRF' : getCookieValue('X-LABKEY-CSRF')
                     }
        });
    },
    getOntoMapping : function(form){
        return fetch(`${baseUrl}${containerPath}/ontologymanagement-getOntoMapping.api`, {
               credentials: "same-origin",
               method: 'POST',
               body: JSON.stringify(form),
               headers: {
                'content-type': 'application/json',
                'X-LABKEY-CSRF' : getCookieValue('X-LABKEY-CSRF')
             }
        });
    },
    postGetDataViews : function(onSuccess){
        LABKEY.Query.getDataViews({
            dataTypes: ["datasets"],
            success:onSuccess,
            failure: this.onError,
        });
    },
}


export const ReactTableLabkey = {

    getLabkeyData : function(onSuccess, onFailure, schema, query){
        LABKEY.Query.selectRows({
            schemaName: schema,
            queryName: query,
            success: onSuccess,
            failure: onFailure,
            includeDetailsColumn :true
        });
    },
    getOntologyAnnotationsData : function(onSuccess, onFailure, schema, query){
        LABKEY.Query.selectRows({
            schemaName: "ontologymanagement",
            queryName: "ontologymapping",
            success: onSuccess,
            failure: onFailure,
            columns:["ontologyid","concepturi","fieldname","conceptlabel"],
            filterArray: [
                LABKEY.Filter.create('schemaname', schema),
                LABKEY.Filter.create('queryname', query)
            ]
        });
    },
    getUserAvailableAnnotationsData : function(onSuccess, onFailure){
        LABKEY.Query.selectRows({
            schemaName: "ontologymanagement",
            queryName: "ontologymapping",
            success: onSuccess,
            failure: onFailure,
            columns:["ontologyid","conceptlabel","queryname","fieldname","concepturi","Container"],
            containerFilter:LABKEY.Query.containerFilter.allFolders,
            includeDetailsColumn:true,
        });
    }

}


