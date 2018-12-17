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
import * as ReactDOM from 'react-dom'
import $ from 'jquery'
import {API,ReactTableLabkey}                  from '../api'
import ReactTable from "react-table";
import "react-table/react-table.css";
import {MappingDropdown} from '../components/form/mappingDropdown'
import {CSVDownload,CSVLink} from 'react-csv';

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */


const onApiFailure = (errorInfo,  options, responseObj) =>{
    if (errorInfo && errorInfo.exception)
        alert("Failure:" + errorInfo.exception)
    else
        alert("Failure:" + responseObj.statusText)
}






export class OntologyMan extends React.Component {

    constructor(props)
    {
        super(props);
        this.state = {
            dataViewList:[],
            schema:"Schema",
            query:"Query",
            data: null,
            columns: null,
            csvData:null
        }

        this.renderTable = this.renderTable.bind(this);
        this.getDataViews = this.getDataViews.bind(this);
        this.getOntologyAnnotations = this.getOntologyAnnotations.bind(this);
        this.convertToReactColumns = this.convertToReactColumns.bind(this);


        

    }

    convertToReactColumns(data){
        var columns = [];
        for (var columnIndex in data.columnModel){

            columns.push({
                accessor:data.columnModel[columnIndex].dataIndex,
                minWidth:160,
                Header: MappingDropdown(data.columnModel[columnIndex].dataIndex,data.queryName,data.schemaName,this.getOntologyAnnotations)
                
            });
        }
         return columns;
    }

    componentDidMount(){
        this.getDataViews();
    }

    getOntologyAnnotations(){
        let _this = this
        function onSuccess(data){
            console.log(data);
            _this.setState({csvData:data.rows})
        }
        ReactTableLabkey.getOntologyAnnotationsData(onSuccess, onApiFailure,this.state.schema,this.state.query)
    }

    getData(){
        let _this = this
        function onSuccess(data){
            console.log(data);
            var tableData = _this.convertToReactColumns(data);
            console.log(tableData);
            _this.setState({columns : tableData});
            _this.setState({data : data.rows});
            _this.getOntologyAnnotations();
        }
        ReactTableLabkey.getLabkeyData(onSuccess, onApiFailure,this.state.schema,this.state.query)
    }


    handleData(queryName) {
        this.setState({query:queryName},()=>{this.getData()});
    }

    getDataViews(){
        let _this = this
        function onDataViewSuccess(data){
           _this.setState({dataViewList:data}) 
           console.log(data)
        }
        API.postGetDataViews(onDataViewSuccess);
    }

    renderMenu(){
        const columnsList = [
            {accessor: "schemaName",Header:"Schema",show:false},
            {accessor:"queryName", Header:"Select Dataset",width:160,style:{cursor: "pointer"}, headerStyle:{overflow:"hidden"} ,
                filterMethod:(filter, row, column) => {const id = filter.pivotId || filter.id 
                    return row[id] !== undefined ? String(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
                },
                Cell:row =>(<div  style={{ borderRadius: "2px",backgroundColor:row.value == this.state.query ?"lightblue":"inherit"}}>{row.value}</div>)
            }
        ]
            return (
                <ReactTable
                    filterable
                    data={this.state.dataViewList}
                    columns={columnsList}
                    showPagination = {false}
                    pageSize={this.state.dataViewList.length}
                    className="-striped -highlight"
                    getTdProps={(state, rowInfo, column, instance) => {
                        return {
                          onClick: (e, handleOriginal) => {
                              if(column.id == "queryName" && rowInfo.original){
                                  console.log(rowInfo)
                                this.setState({schema:rowInfo.original.schemaName})
                                this.setState({query:rowInfo.original.queryName},()=>{this.getData()});
                              }
                           if (handleOriginal) {
                                handleOriginal();
                              }
                          }
                        }
                    }
                    }    
                />

            )
    }

    renderTable(){
        if(this.state.data){
            return (
                <ReactTable
                    data={this.state.data}
                    columns={this.state.columns}
                    defaultPageSize={25}
                    sortable={false}
                    className="-striped -highlight"
                    style={{
                        resize :"both"
                    }}
                />
            )}else{
                return("Please Select a Dataset.")
            }
    }

    renderCsvExport(){
        let _this = this
        // Only test on Google Chrome
        var isChrome = !!window.chrome && !!window.chrome.webstore;

        if(this.state.csvData && isChrome){
            const headers = [
                {label: 'Data Field', key: 'fieldname'},
                {label: 'Concept URI', key: 'concepturi'},
                {label: 'Concept Label', key: 'conceptlabel'},
                {label: 'Ontology Id', key: 'ontologyid'},
                {label: 'Annotation URL', key: '_labkeyurl_ontologyid'},
              ];
            var d = new Date();
            var str_date = d.getFullYear()+"_"+d.getMonth()+"_"+d.getDate()+"_"+d.getHours()+"_"+ d.getMinutes()+"_"+d.getSeconds();
            const fileName = LABKEY.container.name+"_"+this.state.schema+"_"+this.state.query+str_date+"_"+"mappings.csv";
            return(
                 <CSVLink data={this.state.csvData} headers ={headers} filename={fileName}>Download Annotations as CSV </CSVLink>
            )
        }
    }

    renderManagerLink(){
        if(LABKEY.Security.currentUser.isAdmin){
            return(<a href={LABKEY.ActionURL.buildURL("ontologymanagement","listOntologies")} className="labkey-button">Manage Ontologies</a>)
        }
    }



    render(){
        return (
             <div className= "webpartMainDiv">
                {this.renderManagerLink()}
                <h4>Annotate Dataset: {this.renderCsvExport()} </h4>
                <div className="onto-row">
                  <div className="onto-column">{this.renderMenu()}</div><div className="onto-column"> {this.renderTable()}</div>
                </div>
             </div>

        )
    }
}

$(() => ReactDOM.render(<OntologyMan/>, document.getElementById('ontologyman')));