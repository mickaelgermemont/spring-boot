/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.boot.bind;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link RelaxedPropertyResolver}.
 * 
 * @author Phillip Webb
 */
public class RelaxedPropertyResolverTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private StandardEnvironment environment;

	private RelaxedPropertyResolver resolver;

	private LinkedHashMap<String, Object> source;

	@Before
	public void setup() {
		this.environment = new StandardEnvironment();
		this.source = new LinkedHashMap<String, Object>();
		this.source.put("myString", "value");
		this.source.put("myInteger", 123);
		this.source.put("myClass", "java.lang.String");
		this.environment.getPropertySources().addFirst(
				new MapPropertySource("test", this.source));
		this.resolver = new RelaxedPropertyResolver(this.environment);
	}

	@Test
	public void needsPropertyResolver() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("PropertyResolver must not be null");
		new RelaxedPropertyResolver(null);
	}

	@Test
	public void getRequiredProperty() throws Exception {
		assertThat(this.resolver.getRequiredProperty("my-string"), equalTo("value"));
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("required key [my-missing] not found");
		this.resolver.getRequiredProperty("my-missing");
	}

	@Test
	public void getRequiredPropertyWithType() throws Exception {
		assertThat(this.resolver.getRequiredProperty("my-integer", Integer.class),
				equalTo(123));
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("required key [my-missing] not found");
		this.resolver.getRequiredProperty("my-missing", Integer.class);
	}

	@Test
	public void getProperty() throws Exception {
		assertThat(this.resolver.getProperty("my-string"), equalTo("value"));
		assertThat(this.resolver.getProperty("my-missing"), nullValue());
	}

	@Test
	public void getPropertyWithDefault() throws Exception {
		assertThat(this.resolver.getProperty("my-string", "a"), equalTo("value"));
		assertThat(this.resolver.getProperty("my-missing", "a"), equalTo("a"));
	}

	@Test
	public void getPropertyWithType() throws Exception {
		assertThat(this.resolver.getProperty("my-integer", Integer.class), equalTo(123));
		assertThat(this.resolver.getProperty("my-missing", Integer.class), nullValue());
	}

	@Test
	public void getPropertyWithTypeAndDefault() throws Exception {
		assertThat(this.resolver.getProperty("my-integer", Integer.class, 345),
				equalTo(123));
		assertThat(this.resolver.getProperty("my-missing", Integer.class, 345),
				equalTo(345));
	}

	@Test
	public void getPropertyAsClass() throws Exception {
		assertThat(this.resolver.getPropertyAsClass("my-class", String.class),
				equalTo(String.class));
		assertThat(this.resolver.getPropertyAsClass("my-missing", String.class),
				nullValue());
	}

	@Test
	public void containsProperty() throws Exception {
		assertThat(this.resolver.containsProperty("my-string"), equalTo(true));
		assertThat(this.resolver.containsProperty("myString"), equalTo(true));
		assertThat(this.resolver.containsProperty("my_string"), equalTo(true));
		assertThat(this.resolver.containsProperty("my-missing"), equalTo(false));
	}

	@Test
	public void resolverPlaceholder() throws Exception {
		this.thrown.expect(UnsupportedOperationException.class);
		this.resolver.resolvePlaceholders("test");
	}

	@Test
	public void resolveRequiredPlaceholders() throws Exception {
		this.thrown.expect(UnsupportedOperationException.class);
		this.resolver.resolveRequiredPlaceholders("test");
	}

	@Test
	public void prefixed() throws Exception {
		this.resolver = new RelaxedPropertyResolver(this.environment, "a.b.c.");
		this.source.put("a.b.c.d", "test");
		assertThat(this.resolver.containsProperty("d"), equalTo(true));
		assertThat(this.resolver.getProperty("d"), equalTo("test"));
	}

	@Test
	public void subProperties() throws Exception {
		this.source.put("x.y.my-sub.a.b", "1");
		this.source.put("x.y.mySub.a.c", "2");
		this.source.put("x.y.MY_SUB.a.d", "3");
		this.resolver = new RelaxedPropertyResolver(this.environment, "x.y.");
		Map<String, Object> subProperties = this.resolver.getSubProperties("my-sub.");
		assertThat(subProperties.size(), equalTo(3));
		assertThat(subProperties.get("a.b"), equalTo((Object) "1"));
		assertThat(subProperties.get("a.c"), equalTo((Object) "2"));
		assertThat(subProperties.get("a.d"), equalTo((Object) "3"));
	}
}
