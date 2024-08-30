/**
 * Copyright 2024 EReS research Lab - Linnaeus University
 * Contact: https://lnu.se/en/research/research-groups/engineering-resilient-systems-eres/
 * 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Contributors: 
 * 		Diego Perez
 */
package se.lnu.eres.fuzzy_assessment_goals.functions.exceptions;

public class FuzzyNumberConversionException extends Exception {

	private static final long serialVersionUID = 4070795359677411824L;


	public FuzzyNumberConversionException() {
		// TODO Auto-generated constructor stub
	}

	public FuzzyNumberConversionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FuzzyNumberConversionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public FuzzyNumberConversionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public FuzzyNumberConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
