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

export class OntologyOverview extends React.Component {

    constructor(props)
    {
        super(props);
        this.state = {
            data: null,
            columns: null,
        }

        this.getData = this.getData.bind(this);
        this.convertToReactColumns = this.convertToReactColumns.bind(this);
        this.renderTable = this.renderTable.bind(this);


    }

    convertToReactColumns(data){
        var columns = [];

        columns.push({
            accessor:"_labkeyurl_Container",
            minWidth:160,
            Header: "Study",
            Aggregated: row => {},
            Cell: (row) => (<span><a href={row.value}>{row.value?unescape(row.value.substr(0, row.value.lastIndexOf("/")).replace(LABKEY.ActionURL.getContextPath(),"")):""}</a></span>)
            
        });

        for (var columnIndex in data.columnModel){
            if(data.columnModel[columnIndex].dataIndex !="Container"){
                columns.push({
                    Aggregated: row => {},
                    accessor:data.columnModel[columnIndex].dataIndex,
                    minWidth:160,
                    Header: data.columnModel[columnIndex].dataIndex,
                    filterMethod:(filter, row, column) => {const id = filter.pivotId || filter.id 
                    return row[id] !== undefined ? String(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
                    }    
                });
            }
            } 
         return columns;
    }

    componentDidMount(){
        console.log("mount");
        this.getData();
    }

    getData(){
        let _this = this
        function onSuccess(data){
            console.log(data);
            var tableData = _this.convertToReactColumns(data);
            console.log(tableData);            
            _this.setState({columns : tableData});
            _this.setState({data : data.rows});
        }
        ReactTableLabkey.getUserAvailableAnnotationsData(onSuccess, onApiFailure)
    }

    render

    renderTable(){
        if(this.state.data){
            return(
            <ReactTable
                filterable
                data={this.state.data}
                columns={this.state.columns}
                defaultPageSize={10}
                sortable={true}
                className="-striped -highlight"
                style={{
                    resize :"both"
                }}
                pivotBy={["ontologyid", "conceptlabel"]}
            />)
        }else{
            return("Loading data....")
        }
    } 

    render(){
        return (
            <div>
                {this.renderTable()}
            </div>

        )
    }
}

$(() => ReactDOM.render(<OntologyOverview/>, document.getElementById('ontologyoverview')));