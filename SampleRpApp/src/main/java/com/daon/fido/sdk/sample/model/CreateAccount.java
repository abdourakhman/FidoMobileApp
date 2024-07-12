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


public class CreateAccount {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
	private boolean registrationRequested;
	private String sponsorshipToken;
	private String language;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSponsorshipCode() {
		return sponsorshipToken;
	}

	public void setSponsorshipCode(String sponsorshipCode) {
		this.sponsorshipToken = sponsorshipCode;
	}
	
	public CreateAccount() {
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public boolean isRegistrationRequested() {
		return registrationRequested;
	}

	public void setRegistrationRequested(boolean registrationRequested) {
		this.registrationRequested = registrationRequested;
	}
}
