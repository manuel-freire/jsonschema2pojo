/**
 * Copyright © 2010-2013 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JFieldVar;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.jsonschema2pojo.Schema;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Applies the "javaAnnotation" schema property.
 * 
 * @see <a
 *      href="https://github.com/joelittlejohn/jsonschema2pojo/issues/107">https://github.com/joelittlejohn/jsonschema2pojo/issues/107</a>
 */
public class AnnotationRule implements Rule<JFieldVar, JFieldVar> {

	private final RuleFactory ruleFactory;

	public AnnotationRule(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

    @Override
    @SuppressWarnings("unchecked")
    public JFieldVar apply(String nodeName, JsonNode node, JFieldVar field, Schema currentSchema) {
	    String fqn = node.asText(); // fully-qualified name
	    field.annotate((Class<? extends Annotation>)getAnnotationClass(fqn));
	    return field;
    }

	private Class<?> getAnnotationClass(String fullyQualifiedClassName) {
		try {
			return Class.forName(fullyQualifiedClassName);
		} catch (Exception e) {
			try {
				return generateDummyClass(fullyQualifiedClassName);
			} catch (Exception e2) {
				// could not instantiate, could not generate -- bail out
				throw new IllegalArgumentException("Could not generate annotation: "
					+ fullyQualifiedClassName);
			}
		}
	}

	/**
	 * From http://stackoverflow.com/questions/17338521/java-codemodel-annotation-with-class-as-value-class-not-on-classpath
	 * @param fullyQualifiedClassName
	 * @return
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	private Class<?> generateDummyClass(String fullyQualifiedClassName)
		throws IOException, CannotCompileException {

		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(this.getClass()));
		CtClass ctClass = pool.makeClass(fullyQualifiedClassName);
		Class<?> clazz = ctClass.toClass();
		return clazz;
	}
}
