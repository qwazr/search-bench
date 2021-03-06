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

import com.qwazr.search.bench.test.TestSettings;
import org.junit.BeforeClass;

import static com.qwazr.search.bench.test.CommonTestSuite.currentResults;

public class NrtReplicationMasterNoCfs extends NrtReplicationBase {

	@BeforeClass
	public static void before() throws Exception {
		NrtReplicationBase.before("masterNoCfs", TestSettings.of(currentResults)
				.schemaDirectory(schemaDirectory)
				.index("masterNoCfs")
				.ramBuffer(256)
				.useCompoundFile(false)
				.settings());
	}

}
