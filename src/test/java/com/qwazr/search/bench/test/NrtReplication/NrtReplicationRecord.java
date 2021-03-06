/*
 * Copyright 2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.bench.test.NrtReplication;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.bench.TtlLineReader;
import com.qwazr.search.bench.test.BaseQwazrRecord;
import com.qwazr.search.bench.test.BaseTest;
import com.qwazr.search.field.FieldDefinition;

/**
 * Created by ekeller on 01/01/2017.
 */
@Index(schema = BaseTest.SCHEMA_NAME, name = BaseTest.INDEX_NAME)
public class NrtReplicationRecord extends BaseQwazrRecord {

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class,
			stored = false)
	@Copy(to = { @Copy.To(order = 1, field = "shortAbstractEn") })
	protected final String shortAbstract;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
			queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class,
			stored = true)
	protected final String shortAbstractEn;

	public NrtReplicationRecord() {
		shortAbstract = null;
		shortAbstractEn = null;
	}

	public NrtReplicationRecord(final TtlLineReader line) {
		super(line);
		shortAbstract = line.object;
		shortAbstractEn = null;
	}

}
