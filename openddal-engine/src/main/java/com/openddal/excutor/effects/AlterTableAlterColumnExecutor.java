/*
 * Copyright 2014-2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Created on 2015年4月12日
// $Id$

package com.openddal.excutor.effects;

import java.util.List;

import com.openddal.command.CommandInterface;
import com.openddal.command.ddl.AlterTableAlterColumn;
import com.openddal.dbobject.table.TableMate;
import com.openddal.excutor.ExecutionFramework;
import com.openddal.excutor.works.UpdateWorker;
import com.openddal.message.DbException;
import com.openddal.route.rule.ObjectNode;
import com.openddal.route.rule.RoutingResult;
import com.openddal.util.New;

/**
 * This executor execute the statements
 * ALTER TABLE ADD,
 * ALTER TABLE ADD IF NOT EXISTS,
 * ALTER TABLE ALTER COLUMN,
 * ALTER TABLE ALTER COLUMN RESTART,
 * ALTER TABLE ALTER COLUMN SELECTIVITY,
 * ALTER TABLE ALTER COLUMN SET DEFAULT,
 * ALTER TABLE ALTER COLUMN SET NOT NULL,
 * ALTER TABLE ALTER COLUMN SET NULL,
 * ALTER TABLE DROP COLUMN
 *
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public class AlterTableAlterColumnExecutor extends ExecutionFramework<AlterTableAlterColumn> {

    private List<UpdateWorker> workers;

    public AlterTableAlterColumnExecutor(AlterTableAlterColumn prepared) {
        super(prepared);
    }



    @Override
    protected void doPrepare() {
        TableMate table = getTableMate(prepared.getTable().getName());
        int type = prepared.getType();
        switch (type) {
        case CommandInterface.ALTER_TABLE_ALTER_COLUMN_NOT_NULL:
        case CommandInterface.ALTER_TABLE_ALTER_COLUMN_NULL:
        case CommandInterface.ALTER_TABLE_ALTER_COLUMN_DEFAULT:
        case CommandInterface.ALTER_TABLE_ALTER_COLUMN_CHANGE_TYPE:
        case CommandInterface.ALTER_TABLE_ADD_COLUMN:
        case CommandInterface.ALTER_TABLE_DROP_COLUMN: {
            RoutingResult rr = routingHandler.doRoute(table);
            ObjectNode[] selectNodes = rr.getSelectNodes();
            workers = New.arrayList(selectNodes.length);
            for (ObjectNode objectNode : selectNodes) {
                UpdateWorker handler = queryHandlerFactory.createUpdateWorker(prepared, objectNode);
                workers.add(handler);
            }
            break;
        }
        case CommandInterface.ALTER_TABLE_ALTER_COLUMN_SELECTIVITY: {
            throw DbException.getUnsupportedException("command ALTER_TABLE_ALTER_COLUMN_SELECTIVITY not support.");
        }
        default:
            throw DbException.throwInternalError("type=" + type);
        }
    }

    @Override
    public int doUpdate() {
        TableMate table = getTableMate(prepared.getTable().getName());
        int affectRows = invokeUpdateWorker(workers);
        table.loadMataData(session);
        return affectRows;
    }

    @Override
    protected String doExplain() {
        return explainForWorker(workers);
    }

}
