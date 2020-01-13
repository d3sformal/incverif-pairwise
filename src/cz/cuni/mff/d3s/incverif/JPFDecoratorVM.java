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
package cz.cuni.mff.d3s.incverif;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import cz.cuni.mff.d3s.incverif.wala.jvm.JVM;


public class JPFDecoratorVM implements JVM
{
	protected String[] jpfModelsClassPath;
	protected JVM decoratedJVM;
	
	public JPFDecoratorVM(String jpfClassPathStr, JVM decorJVM)
	{
		this.jpfModelsClassPath = jpfClassPathStr.split(";");
		this.decoratedJVM = decorJVM;
	}

	public String[] getJarFiles()
	{
		List<String> jarFiles = new ArrayList<String>();
		
		jarFiles.addAll(Arrays.asList(jpfModelsClassPath));
		jarFiles.addAll(Arrays.asList(decoratedJVM.getJarFiles()));
		
		return jarFiles.toArray(new String[]{});
	}
}
