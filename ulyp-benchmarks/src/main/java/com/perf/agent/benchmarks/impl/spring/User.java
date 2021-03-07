package com.perf.agent.benchmarks.impl.spring;

public class User {

	private int id;
	private String firstName;
	private String lastName;
	private int v1;
	private int v2;
	private int v3;

	public User() {
	}

	public User(final String firstName, final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	// getters

	public String getFirstName() {
		return firstName;
	}

	public int getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public int getV1() {
		return v1;
	}

	public int getV2() {
		return v2;
	}

	public int getV3() {
		return v3;
	}

	// setters

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setV1(int v1) {
		this.v1 = v1;
	}

	public void setV2(int v2) {
		this.v2 = v2;
	}

	public void setV3(int v3) {
		this.v3 = v3;
	}
}
