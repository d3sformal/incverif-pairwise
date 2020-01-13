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

import java.util.Properties;

import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.WalaException;


public class DefaultJVMFactory implements JVMFactory
{
	protected Properties walaProps;
	
	public DefaultJVMFactory()
	{
		try
		{
			this.walaProps = WalaProperties.loadProperties();
		}
		catch (WalaException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public JVM getJVM()
	{
		return new OracleJVM(walaProps);
	}
}

