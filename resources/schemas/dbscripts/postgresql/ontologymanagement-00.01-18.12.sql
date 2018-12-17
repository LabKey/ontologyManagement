/*
 * Copyright (c) 2018 Nestec Ltd. 
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


CREATE SCHEMA ontologymanagement;

CREATE TABLE ontologymanagement.ontology
(
    -- standard fields
    _ts TIMESTAMP DEFAULT now(),
    RowId SERIAL,
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,
    Owner USERID NULL,

    -- other fields
    Container ENTITYID NOT NULL,
    Ontologyname TEXT,
    Endpoint TEXT,
    Query TEXT,
    Ontologyid TEXT NOT NULL,
    Importstatus TEXT,
    CONSTRAINT PK_Ontology PRIMARY KEY (RowId)
);

CREATE TABLE ontologymanagement.ontologydata
(
    -- standard fields
    _ts TIMESTAMP DEFAULT now(),
    RowId SERIAL,
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,
    Owner USERID NULL,

    -- other fields
    Container ENTITYID NOT NULL,
    ontologyrun TEXT NOT NULL,
    ontologyid TEXT NOT NULL,
    subject TEXT NOT NULL,
    property TEXT NOT NULL,
    object TEXT NOT NULL,
    labkeyproperty TEXT,
    CONSTRAINT PK_Ontologydata PRIMARY KEY (RowId)
);


CREATE TABLE ontologymanagement.labkeyalign
(
    -- standard fields
    _ts TIMESTAMP DEFAULT now(),
    RowId SERIAL,
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,
    Owner USERID NULL,

    -- other fields
    Container ENTITYID NOT NULL,
    ontologyrun TEXT NOT NULL,
    ontologyprop TEXT NOT NULL,
    labkeyproperty TEXT NOT NULL,
    CONSTRAINT PK_Labkeyalign PRIMARY KEY (RowId)
);

CREATE TABLE ontologymanagement.ontologyMapping
(
    -- standard fields
    _ts TIMESTAMP DEFAULT now(),
    RowId SERIAL,
    CreatedBy USERID,
    Created TIMESTAMP,
    ModifiedBy USERID,
    Modified TIMESTAMP,
    Owner USERID NULL,
    Container ENTITYID NOT NULL,

    -- other fields
    ontologyid TEXT NOT NULL,
    concepturi TEXT NOT NULL,
    fieldname TEXT NOT NULL,
    schemaname TEXT NOT NULL,
    queryname TEXT NOT NULL,
    conceptlabel TEXT NOT NULL,
    CONSTRAINT PK_OntologyMapping PRIMARY KEY (RowId)
);


/*
* Scripts to enable search on ontology concepts
*/


CREATE INDEX "ontologyid_index" ON "ontologymanagement"."ontologydata" USING btree ("ontologyid");
CREATE INDEX "uri_index" ON "ontologymanagement"."ontologydata" USING btree ("subject");

ALTER TABLE ontologymanagement.ontologydata ADD COLUMN weighted_tsv tsvector;

/*####### label #######*/

CREATE FUNCTION ontologymanagement.label_weighted_tsv_trigger() RETURNS trigger AS $$
begin
  new.weighted_tsv :=
     setweight(to_tsvector('english',new.object), 'A');
  return new;
end
$$ LANGUAGE plpgsql;

CREATE TRIGGER label_tsvector BEFORE INSERT OR UPDATE
ON ontologymanagement.ontologydata
FOR EACH ROW
WHEN (NEW.labkeyproperty = 'labkey:label')
EXECUTE PROCEDURE ontologymanagement.label_weighted_tsv_trigger();

/*####### altLabel #######*/
CREATE FUNCTION ontologymanagement.altLabel_weighted_tsv_trigger() RETURNS trigger AS $$
begin
  new.weighted_tsv :=
     setweight(to_tsvector('english',new.object), 'C');
  return new;
end
$$ LANGUAGE plpgsql;

CREATE TRIGGER altLabel_tsvector BEFORE INSERT OR UPDATE
ON ontologymanagement.ontologydata
FOR EACH ROW
WHEN (NEW.labkeyproperty = 'labkey:altLabel')
EXECUTE PROCEDURE ontologymanagement.altLabel_weighted_tsv_trigger();


/*################Create index ################*/
CREATE INDEX weighted_tsv_idx ON ontologymanagement.ontologydata USING GIST (weighted_tsv);

