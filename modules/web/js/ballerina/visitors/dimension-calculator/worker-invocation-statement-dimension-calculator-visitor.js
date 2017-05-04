/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import log from 'log';
import * as DesignerDefaults from './../../configs/designer-defaults';
import {util} from './../sizing-utils';
import _ from 'lodash';
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

class WorkerInvocationStatementDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        util.populateSimpleStatementBBox(node.getInvocationStatement() , node.getViewState());
        let workerDeclaration = node.getDestination();
        if(!_.isUndefined(workerDeclaration)) {
            let heightFromTop = util.getStatementHeightBefore(node);
            let workerFirstStatement = workerDeclaration.findChild(BallerinaASTFactory.isStatement);
            workerFirstStatement.getViewState().components['drop-zone'].h += (heightFromTop + util.getDefaultStatementHeight());
            workerFirstStatement.getViewState().bBox.h += (heightFromTop + util.getDefaultStatementHeight());
        }
    }
}

export default WorkerInvocationStatementDimensionCalculatorVisitor;
