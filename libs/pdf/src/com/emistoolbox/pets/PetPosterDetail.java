package com.emistoolbox.pets;

public class PetPosterDetail {
	String id; // ID of information - breed, name
	String title; // E. g. Breed, Name (changes with language)
	String value; // E. g. Poodle, Fifi (changes with language)
	int size; // 1 for normal value field, 2 for bigger value field
	
	public PetPosterDetail (String id,String title,String value,int size) {
		this.id = id;
		this.title = title;
		this.value = value;
		this.size = size;
	}
}
