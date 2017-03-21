package com.restapi.controller;
/**
 * File path handling controller
 * @param listUploadedFiles	GetMapping("/")
 * @param loadPropertiesFile2 	GetMapping(value = "/files/prop/{filename:.+}")
 * @param serveFile		GetMapping("/files/{filename:.+}")
 * @param handleUpload	RequestMapping("/files")
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restapi.services.MetadataService;
import com.restapi.services.StorageService;
import com.restapi.utils.StorageNotFoundException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files",
				storageService.loadAll()
						// .filter(path -> path.endsWith(".properties"))
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(FileUploadController.class, "loadPropertiesFile",
										path.getFileName().toString())
								.build().toString())
						.collect(Collectors.toList()));
		return "uploadfile";
	}

	@GetMapping(value = "/files/prop/{filename:.+}")
	public String loadPropertiesFile2(@PathVariable String filename, Model model) {
		Resource file;
		file = storageService.loadAsResource(filename + ".metadata.properties");
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(file.getFile()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> ls = new ArrayList<String>();
		ls.add(prop.getProperty("file-name"));
		ls.add(prop.getProperty("person-name"));
		ls.add(prop.getProperty("extra-notes"));
		ls.add(prop.getProperty("document-date"));
		ls.add(prop.getProperty("uuid"));

		model.addAttribute("props", ls);
		return "uploadfile";

	}

	@GetMapping(value = "/files/props/{filename:.+}")
	@ResponseBody
	public ResponseEntity<List<String>> loadPropertiesFile(@PathVariable String filename) {
		Resource file;
		if (filename.endsWith(".metadata.properties")) {
			file = storageService.loadAsResource(filename);
			Properties prop = new Properties();
			try {
				prop.load(new FileReader(file.getFile()));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Map<String, String> map;//return a list of certain order
			List<String> ls = new ArrayList<String>();
			ls.add(prop.getProperty("file-name"));
			ls.add(prop.getProperty("person-name"));
			ls.add(prop.getProperty("extra-notes"));
			ls.add(prop.getProperty("document-date"));
			ls.add(prop.getProperty("uuid"));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
					.body(ls);
		} else
			return null;
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("person") String person,
			@RequestParam("extranotes") String extranotes, RedirectAttributes redirectAttributes) {

		Date date = new Date();
		MetadataService metadata = new MetadataService(file.getOriginalFilename(), extranotes, date, person);
		storageService.store(file, metadata);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded file: " + file.getOriginalFilename() + "!");

		return "redirect:/files/prop/" + file.getOriginalFilename();
	}

	@SuppressWarnings("rawtypes")
	@ExceptionHandler(StorageNotFoundException.class)
	public ResponseEntity handleStorageFileNotFound(StorageNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
