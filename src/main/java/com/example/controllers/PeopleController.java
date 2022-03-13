package com.example.controllers;

import com.example.people.PeopleDAO;
import com.example.people.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class PeopleController {

    @Autowired
    private PeopleDAO peopleDAO;

    @ModelAttribute("headerMessage")
    public String getHeaderMessage() {
        return "Welcome to our website!";
    }

    @GetMapping("/people")
    public String getPeople(Model model) {
        model.addAttribute("people", peopleDAO.getPeopleList());
        if (peopleDAO.getPeopleList().size() > 0) {
            model.addAttribute("lastAddedPerson", peopleDAO.getPeopleList().get(peopleDAO.getPeopleList().size() - 1));
        } else {
            model.addAttribute("lastAddedPerson", new Person("Nobody has been added", "", 0, ""));
        }
        return "people/list";
    }

    @GetMapping("/people/{id}")
    public String getPerson(@PathVariable("id") int id, Model model) {
        model.addAttribute("person", peopleDAO.getPersonById(id));
        return "people/person";
    }

    @DeleteMapping("/people/{id}")
    public String deletePerson(@PathVariable("id") int id) {
        peopleDAO.deletePerson(id);
        return "redirect:/people";
    }

    @GetMapping("people/new")
    public String newPerson(Model model) {
        model.addAttribute("person", new Person());
        return "people/newpersonThymeleaf";
    }

    @PostMapping("/people")
    public String createPerson(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "people/newpersonThymeleaf";

        peopleDAO.addPerson(person);
        return "redirect:/people";
    }

    @GetMapping("/people/{id}/edit")
    public String editPerson(@PathVariable("id") int id, Model model) {
        model.addAttribute("person", peopleDAO.getPersonById(id));
        return "people/edit";
    }

    @PatchMapping("/people/{id}")
    public String updatePerson(@PathVariable("id") int id,
                               @ModelAttribute("person") @Valid Person person, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "people/edit";

        peopleDAO.update(id, person);
        return "redirect:/people";
    }
}
