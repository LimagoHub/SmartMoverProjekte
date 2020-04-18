package de.ing.fte_statistics.email;

/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.annotation.Transformer;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses the E-mail Message and converts each containing message and/or attachment into
 * a {@link List} of {@link EmailFragment}s.
 * 
 * Logger changed by Jo Wagner
 *
 * @author Gunnar Hillert
 * @author Gary Russell
 * @since 2.2
 *
 */
@Slf4j
public class EmailTransformer {

	

	@Transformer
	public List<EmailFragment> transformit(javax.mail.Message mailMessage) {

		final List<EmailFragment> emailFragments = new ArrayList<EmailFragment>();

		EmailParserUtils.handleMessage(null, mailMessage, emailFragments);

		
		log.info(String.format("Email contains %s fragments.", emailFragments.size()));
		

		return emailFragments;
	}

}