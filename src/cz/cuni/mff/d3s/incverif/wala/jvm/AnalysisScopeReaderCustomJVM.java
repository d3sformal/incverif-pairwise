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

import java.util.StringTokenizer;
import java.util.jar.JarFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;


/**
 * This code is heavily based on the original implementation from the WALA libraries (IBM).
 */ 
public class AnalysisScopeReaderCustomJVM
{
	public static JVMFactory getDefaultJVMFactory() 
	{
		return new DefaultJVMFactory();
	}
	
	public static AnalysisScope readJavaScope(String scopeFileName, File exclusionsFile, ClassLoader javaLoader, JVMFactory jvmFactory) throws IOException
	{
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		
		return read(scope, scopeFileName, exclusionsFile, javaLoader, new FileProvider(), jvmFactory);
	}

	protected static AnalysisScope read(AnalysisScope scope, String scopeFileName, File exclusionsFile, ClassLoader javaLoader, FileProvider fp, JVMFactory jvmFactory) throws IOException
	{
		InputStream scopeFileIS = fp.getInputStreamFromClassLoader(scopeFileName, javaLoader);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(scopeFileIS, "UTF-8"));
		
		String line;
		while ((line = br.readLine()) != null)
		{
			processScopeDefLine(scope, javaLoader, jvmFactory, line);
		}
		
		if (exclusionsFile != null)
		{
			InputStream fs = null;
			if (exclusionsFile.exists()) fs = new FileInputStream(exclusionsFile);
			else fs = FileProvider.class.getClassLoader().getResourceAsStream(exclusionsFile.getName());
			
			scope.setExclusions(new FileOfClasses(fs));
		}

		br.close();
		
		return scope;
	}
	
	public static void processScopeDefLine(AnalysisScope scope, ClassLoader javaLoader, JVMFactory jvmFactory, String line) throws IOException
	{
		StringTokenizer st = new StringTokenizer(line, "\n,");
		if ( ! st.hasMoreTokens() ) return;

		Atom loaderName = Atom.findOrCreateUnicodeAtom(st.nextToken());
		ClassLoaderReference walaLoader = scope.getLoader(loaderName);

		String progLanguage = st.nextToken();
		String entryType = st.nextToken();
		String entryPathName = st.nextToken();
		
		FileProvider fp = new FileProvider();
		
		if (entryType.equals("binaryDir"))
		{
			File bd = fp.getFile(entryPathName, javaLoader);
			scope.addToScope(walaLoader, new BinaryDirectoryTreeModule(bd));
		}
		
		if (entryType.equals("jarFile"))
		{
			Module jf = fp.getJarFileModule(entryPathName, javaLoader);
			scope.addToScope(walaLoader, jf);
		}
		
		if (entryType.equals("stdlib"))
		{
			String[] stdlibJars = jvmFactory.getJVM().getJarFiles();
			
			for (int i = 0; i < stdlibJars.length; i++)
			{
				scope.addToScope(walaLoader, new JarFile(stdlibJars[i]));
			}
		}
	}
}

