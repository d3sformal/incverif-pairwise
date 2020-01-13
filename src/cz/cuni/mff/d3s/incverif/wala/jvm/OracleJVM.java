/*
 * Copyright (C) 2020, Charles University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.cuni.mff.d3s.incverif.wala.jvm;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import com.ibm.wala.properties.WalaProperties;


public class OracleJVM implements JVM
{
	protected Properties walaProps;

	public OracleJVM(Properties props)
	{
		this.walaProps = props;
	}

	public String[] getJarFiles()
	{
		String dir = walaProps.getProperty(WalaProperties.J2SE_DIR);
		
		String[] allJars = WalaProperties.getJarsInDirectory(dir);
		
		// filter alt-rt.jar (optimized collections)
		
		List<String> filteredJars = new ArrayList<String>();

		for (String jarFile : allJars)
		{
			if ( ! jarFile.endsWith("alt-rt.jar") ) filteredJars.add(jarFile);
		}
		
		return filteredJars.toArray(new String[]{});
	}
}

