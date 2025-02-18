/*
* Copyright Daon.
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

package com.daon.fido.sdk.sample.model;

public class CreateSession {

	private String email;
	private String password;
	private String fidoAuthenticationResponse;
	private String authenticationRequestId;
	
	public CreateSession() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFidoAuthenticationResponse() {
		return fidoAuthenticationResponse;
	}

	public void setFidoAuthenticationResponse(String fidoAuthenticationResponse) {
		this.fidoAuthenticationResponse = fidoAuthenticationResponse;
	}

	public String getAuthenticationRequestId() {
		return authenticationRequestId;
	}

	public void setAuthenticationRequestId(String authenticationRequestId) {
		this.authenticationRequestId = authenticationRequestId;
	}

}
