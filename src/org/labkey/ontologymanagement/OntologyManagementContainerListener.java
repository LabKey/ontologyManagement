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

package org.labkey.ontologymanagement;

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.RuntimeSQLException;
import org.labkey.api.security.User;
import org.labkey.ontologymanagement.model.OntologyManagementManager;

import java.sql.SQLException;

/**
 * @author rdpintopra
 * Date: Oct 04,2018
 */

public class OntologyManagementContainerListener extends ContainerManager.AbstractContainerListener
{
    public void containerDeleted(Container c, User user)
    {
        try
        {
            OntologyManagementManager.getInstance().deleteAllData(c);
        }
        catch (SQLException e)
        {
            throw new RuntimeSQLException(e);
        }
    }

}
