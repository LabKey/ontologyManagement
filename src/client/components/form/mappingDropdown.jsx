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

import * as React from 'react'
import {API} from '../../api'

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

export class DropdownComponent extends React.Component {
    constructor(props)
    {
        super(props);
        this.state = {
            isVisible: false,
            input: props.field,
            list : [],
            mapping: []
        }
        this.field = props.field
        this.SearchFunction = this.SearchFunction.bind(this)
        this.setNewConcept = this.setNewConcept.bind(this)
        this.toggleVisible = this.toggleVisible.bind(this)
        this.handleChange = this.handleChange.bind(this)
        this.searchTerm = this.searchTerm.bind(this)
        this.getOntoMapping = this.getOntoMapping.bind(this)
        this.schemaname = props.schemaname;
        this.queryname = props.queryname;
        this.addConceptConfirmation = this.addConceptConfirmation.bind(this)
    }

    insertOntopMapping(uri,ontologyid,conceptLabel){
        //Refresh The annotations in the parent component 
        this.props.getOntologyAnnotations();
        API.postInsertOntoMapping({
            "concepturi": uri,
            "fieldname":this.field,
            "schemaname":this.schemaname,
            "queryname":this.queryname,
            "ontologyid":ontologyid,
            "conceptlabel":conceptLabel,
        })
        .then(() => {this.getOntoMapping()})
  
    }
    deleteOntopMapping(rowid){
        //Refresh The annotations in the parent component
        this.props.getOntologyAnnotations();
        API.postDeleteOntoMapping({
            "rowId": rowid,
        })
        .then(() => {this.getOntoMapping()})
    } 

    SearchFunction(){
        return (
            <div>
                <div>
                    <p><b>Assigned Concepts:</b></p>
                    <ul>                    
                    {this.state.mapping.map((res) => 
                        <li key={res._row} className="parent">
                            <i className="fa fa-close" onClick ={()=> this.deleteOntopMapping(res.rowid)}></i> <a id={res.concepturi}>{res.conceptlabel}</a>
                                <ul className="child">
                                    <b>Concept Summary for: <a href={res.concepturi} target="_blank">{res.concepturi}</a></b><i className="fa fa-external-link"></i>
                                    {res.conceptDescription.map((conceptDescription) =>
                                        <li className = "desciptionElement" key={conceptDescription._row}>
                                            <b>{conceptDescription.labkeyproperty}</b>
                                            <p>{conceptDescription.object}</p>
                                        </li>
                                    )}
                                </ul>
                        </li>
                    )}
                </ul>
                </div>
                <div>
                    <p><b>Search Concept:</b></p>
                    <ul>
                    <input type="text" onChange={this.handleChange} name="SearchTerm" value={this.state.input}></input><button onClick={this.addConceptConfirmation}>New</button>                    
                        {this.state.list.map((res) => 
                            <li key={res._row} className="parent">
                                <i className="fa fa-save"   onClick ={()=> this.insertOntopMapping(res.subject,res.ontologyid,res.object)}></i> <a id={res.subject}>{res.object}</a>
                                    <ul className="child">
                                        <b>Concept Summary for: <a href={res.subject} target="_blank">{res.subject}</a></b><i className="fa fa-external-link"></i>
                                        {res.conceptDescription.map((conceptDescription) =>
                                            <li className = "desciptionElement" key={conceptDescription._row}>
                                                <b>{conceptDescription.labkeyproperty}</b>
                                                <p>{conceptDescription.object}</p>
                                            </li>
                                        )}
                                    </ul>
                            </li>
                        )}
                    </ul>
                </div>
            </div>
        )
    }

    searchTerm(){
        API.searchTerm(this.state.input)
        .then((response) => response.json())
        .then(data => {
            this.setState({ list: data })
          })
        .catch(err => {
            console.log(err)
          });
    }

    setNewConcept(){
        var controller = "query"
        var action = "detailsQueryRow"
        var container = LABKEY.ActionURL.getContainer()
        var parameters = {"schemaName":"ontologymanagement","query.queryName":"ontologydata","RowId":""}
        
        API.postInsertNewConcept({
            subject:LABKEY.ActionURL.buildURL(controller, action, container, parameters),
            labkeyproperty:"labkey:label",
            object:this.state.input,
            ontologyid:LABKEY.ActionURL.getContainer()+"/"+this.schemaname+"/"+this.queryname,
            property: "http://www.w3.org/2000/01/rdf-schema#label"

        }).then((response) => response.json())
        .then(data => {
            this.searchTerm();
        }); 
    }
    addConceptConfirmation() {
        var r = confirm("Do you want to create a new concept with the label: "+this.state.input);
        if (r == true) {
            this.setNewConcept();
        }
    }
    getOntoMapping(){

        API.getOntoMapping({
            "fieldname":this.field,
            "schemaname":this.schemaname,
            "queryname":this.queryname,
        })
        .then((response) => response.json())
        .then(data => {
            this.setState({ mapping: data });                
            this.searchTerm();
          })
        .catch(err => {
            console.log(err)
          });
    }


    handleChange(event) {
        this.setState({input:  event.target.value},()=>{this.searchTerm()});
    }
    
    toggleVisible(){
        this.setState(state => ({ isVisible: !state.isVisible }));
        // Refresh the search results
        if(!this.state.isVisible){this.getOntoMapping()}
    } 

    render(){
        return (
            <div className="dropdown">   
                <button className="dropbtn" style ={this.state.isVisible?({backgroundColor:"lightblue"}): ({})} onClick={this.toggleVisible}><i className="fa fa-angle-down"></i> {this.field}</button>
                <div id={this.field} className={"dropdown-content"}>
                    {this.state.isVisible?( this.SearchFunction()): ('')}
                </div>
            </div>
        );
    }
}

export function MappingDropdown(field,queryname,schemaname,getOntologyAnnotations){
    return (<DropdownComponent getOntologyAnnotations ={getOntologyAnnotations} field={field} queryname={queryname} schemaname={schemaname}/>)
}
