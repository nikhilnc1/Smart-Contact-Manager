package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;


	//common data method
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String userName = principal.getName();
		System.out.println("username : "+userName);

		User user =userRepository.getUserByUserName(userName);
		System.out.println("USER : "+ user);

		model.addAttribute("user",user);
	}

	//home handler
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{


		return "normal/user_dashboasrd";
	}

	//open add form handler

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}

	//processing add contact form

	@PostMapping("process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
								 @RequestParam("profileImage") MultipartFile file,
								 Principal principal, HttpSession session)
	{
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);

			//processing and uploading file

			if(file.isEmpty())
			{
				//if the file is empty then try our message
				System.out.println("file is empty");
				session.setAttribute("message",new Message("Something went wrong","alert"));
//				System.out.println("File is empty");
				contact.setImage("contact.png");
			}
			else {
				//file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
//				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get("C:\\Users\\HP\\springeclipse_workspace\\SmartContactsManager-1\\target\\classes\\static\\img"+File.separator+file.getOriginalFilename()  );
				System.out.println(path.toString());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
				session.setAttribute("message",new Message("Your Contact is added successfully","success"));

			}

			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("data "+ contact);
			System.out.println("Added to data base");
		}
		catch (Exception e)
		{
			System.out.println("Error : "+e.getMessage());
			e.printStackTrace();
		}

		return "normal/add_contact_form";
	}


	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");

		String userName = principal.getName();

		User user = this.userRepository.getUserByUserName(userName);

		// currentPage-page
		// Contact Per page - 5
		Pageable pageable = PageRequest.of(page, 8);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}



	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}


	// delete contact handler

	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,
								Principal principal) {
		System.out.println("CID " + cId);

		Contact contact = this.contactRepository.findById(cId).get();


		contact.setUser(null);

		User user = this.userRepository.getUserByUserName(principal.getName());

		user.getContacts().remove(contact);

		this.userRepository.save(user);

		System.out.println("DELETED");

		return "redirect:/user/show-contacts/0";
	}


	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}


}
