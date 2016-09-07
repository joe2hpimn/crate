/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.analyze.relations;

import io.crate.analyze.AnalysisMetaData;
import io.crate.analyze.ParameterContext;
import io.crate.exceptions.ValidationException;
import io.crate.metadata.*;
import io.crate.operation.operator.OperatorModule;
import io.crate.sql.parser.SqlParser;
import io.crate.test.integration.CrateUnitTest;
import io.crate.testing.MockedClusterServiceModule;
import io.crate.testing.T3;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.TimeUnit;

import static io.crate.testing.TestingHelpers.newMockedThreadPool;
import static org.mockito.Mockito.mock;

public class RelationAnalyzerTest extends CrateUnitTest {

    private RelationAnalyzer relationAnalyzer;
    private StatementAnalysisContext statementAnalysisContext;
    private ThreadPool threadPool;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        threadPool = newMockedThreadPool();
        ModulesBuilder modulesBuilder = new ModulesBuilder()
            .add(new MockedClusterServiceModule())
            .add(new OperatorModule())
            .add(T3.META_DATA_MODULE)
            .add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ThreadPool.class).toInstance(threadPool);
            }
        });
        Injector injector = modulesBuilder.createInjector();
        NestedReferenceResolver referenceResolver = new NestedReferenceResolver() {
            @Override
            public ReferenceImplementation<?> getImplementation(Reference refInfo) {
                throw new UnsupportedOperationException("getImplementation not implemented");
            }
        };
        AnalysisMetaData analysisMetaData = new AnalysisMetaData(
            injector.getInstance(Functions.class), injector.getInstance(Schemas.class), referenceResolver);
        relationAnalyzer = new RelationAnalyzer(mock(ClusterService.class), analysisMetaData);
        statementAnalysisContext = new StatementAnalysisContext(mock(ParameterContext.class), new StmtCtx(), analysisMetaData);
    }

    @After
    public void tearDownThreadPool() throws Exception {
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.SECONDS);
    }

    private AnalyzedRelation analyze(String sql) {
        return relationAnalyzer.analyze(SqlParser.createStatement(sql), statementAnalysisContext);
    }

    @Test
    public void testValidateUsedRelationsInJoinConditions() throws Exception {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("missing FROM-clause entry for table 't3'");
        analyze("select * from t1 join t2 on t1.a = t3.c join t3 on t2.b = t3.c");
    }
}
