package com.swadhyaydata.app.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadhyaydata.app.entity.CityZipDTO;
import com.swadhyaydata.app.entity.CommunityZipDTO;
import com.swadhyaydata.app.entity.Contact;
import com.swadhyaydata.app.entity.VisharZipDTO;


@RestController
@RequestMapping(value = "/api")

public class DivyaSetuUIController {

	@Autowired
	DivyaSetuUIControllerService divyaSetuUIControllerService;

	@GetMapping(value = "/test")
	public ResponseEntity<String> test() {

		String str = "<html> <body><center><b>SWADHYAY DATA APP  WORKING FINE   </b></center></body></html>";

	
		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

	@GetMapping("/zipandcity")
	public ResponseEntity<LinkedList<CityZipDTO>> findAllZipAndCity() {
		return ResponseEntity.ok().body(divyaSetuUIControllerService.findAllZipAndCity());
	}

	@GetMapping("/vishar")
	public ResponseEntity<LinkedList<String>> findAllVisharAndZip(
			@RequestParam(required = false) String kendraName) {
		return ResponseEntity.ok().body(divyaSetuUIControllerService.findAllVisharAndZip(kendraName));
	}

	@GetMapping("/zips")
	public LinkedList<String> getAllZip() {
		return divyaSetuUIControllerService.getAllZip();
	}

	@GetMapping("/communities/zip/{zip}")
	public LinkedList<String> getCommunitiesByZip(@PathVariable String zip) {
		return divyaSetuUIControllerService.getCommunitiesByZip(zip);
	}

	@GetMapping("/communities/vishar/{vishtar}")
	public LinkedList<CommunityZipDTO> getCommunitiesByVishar(@PathVariable String vishtar) {
		return divyaSetuUIControllerService.getCommunitiesByVishar(vishtar);
	}
	
	@GetMapping("/communities")
	public LinkedList<String> getCommunities() {
		return divyaSetuUIControllerService.getCommunities();
	}

	@GetMapping("/communities/kendranames")
	public LinkedList<String> getDistinctKendraNames() {
		return divyaSetuUIControllerService.getDistinctKendraNames();
	}

	@GetMapping(value = "/contacts/zip/{zip}/community/{communityName}")
	public ResponseEntity<LinkedList<Contact>> getContactsByCommunityAndZip(@PathVariable String communityName,
			@PathVariable String zip, @RequestParam String commentYear) throws Exception {

		System.out.println(communityName);
		return ResponseEntity.ok()
				.body(divyaSetuUIControllerService.getContactsByCommunityAndZip(communityName, zip, commentYear));

	}

	@GetMapping(value = "/contacts/zip/{zip}")
	public ResponseEntity<LinkedList<Contact>> getContactsByZip(@PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().body(divyaSetuUIControllerService.getContactsByZip(zip, commentYear));

	}

	@PostMapping(value = "/updatecontacts")
	public ResponseEntity<LinkedList<Contact>> updateConatacts(@RequestBody List<Contact> contacts,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().body(divyaSetuUIControllerService.updateContact(contacts, commentYear));

	}

	@PostMapping(value = "/addcontact")
	public ResponseEntity<String> addConatact(@RequestBody Contact contacts) throws Exception {

		return ResponseEntity.ok().body(divyaSetuUIControllerService.addContact(contacts));

	}

	@GetMapping(value = "/folder/zip/{zip}/community/{communityName}")
	public ResponseEntity<byte[]> createFolder(@PathVariable String communityName, @PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		System.out.println(communityName);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + communityName + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(divyaSetuUIControllerService.createFolderByZip(communityName, zip, commentYear));

	}
	
	
	@GetMapping(value = "/folder/zip/{zip}/community/{communityName}/withdetails")
	public ResponseEntity<byte[]> createFolderWithDetails(@PathVariable String communityName, @PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		System.out.println(communityName);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + communityName + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(divyaSetuUIControllerService.createFolderByZipWithCommunityName(communityName, zip, commentYear));

	}

	@GetMapping(value = "/folder/zip/{zip}")
	public ResponseEntity<byte[]> createFolderByZipOnly(@PathVariable String zip, @RequestParam String commentYear)
			throws Exception {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zip + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(divyaSetuUIControllerService.createFolderByZipOnly(zip, commentYear));

	}

	@GetMapping(value = "/sanantonio/folder/zip/{zip}")
	public ResponseEntity<byte[]> createFolderByZipSanantoniaOnly(@PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zip + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(divyaSetuUIControllerService.createFolderByZipSanantoniaOnly(zip, commentYear));

	}



}
