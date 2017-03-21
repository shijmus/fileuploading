package com.restapi.controller;
/**
 * Error/Exception handler
 * @param errorHandler RequestMapping("/error")
 */
import java.io.File;
import java.nio.file.Paths;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restapi.utils.StorageProperties;

@Controller
@Service
public class IndexController implements ErrorController {

	@SuppressWarnings("unused")
	private RedirectAttributes redirectAttributes;

	@RequestMapping(value = "/error", method = RequestMethod.POST)
	public String errorHandler(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		if (file.getOriginalFilename().length() == 0) {
			redirectAttributes.addFlashAttribute("message", "Please specify a file to upload");
		} else if (new File(
				Paths.get(new StorageProperties().getLocation()).resolve(file.getOriginalFilename()).toString())
						.exists()) {
			redirectAttributes.addFlashAttribute("message", "Your file: " + file.getOriginalFilename()
					+ " already exists, please choose a valid file to upload!");

		} else if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message",
					"Your file: " + file.getOriginalFilename() + " is empty, please choose a valid file to upload!");
		} else if (file.getSize() > 5242880) {
			redirectAttributes.addFlashAttribute("message", "Your file: " + file.getOriginalFilename()
					+ " is too igb, please choose a file smaller than 5 MB to upload!");

		}

		return "redirect:/";
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
