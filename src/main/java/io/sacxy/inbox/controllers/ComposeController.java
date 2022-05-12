package io.sacxy.inbox.controllers;


import io.sacxy.inbox.email.EmailService;
import io.sacxy.inbox.folders.Folder;
import io.sacxy.inbox.folders.FolderRepository;
import io.sacxy.inbox.folders.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private EmailService emailService;

    @GetMapping(value = "/compose")
    public String getComposePage(@AuthenticationPrincipal OAuth2User principal, Model model,@RequestParam(required = false) String to) {
        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        } else {


            // Fetch Folders
            String userId = principal.getAttribute("login");
            List<Folder> userFolders = folderRepository.findAllById(userId);
            model.addAttribute("userFolders", userFolders);

            List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
            model.addAttribute("defaultFolders", defaultFolders);
            List<String> uniqueToIds = splitIds(to);

            model.addAttribute("toIds", String.join(", ", uniqueToIds));

            return "compose-page";
        }
    }

    private List<String> splitIds(String to) {
        if(!StringUtils.hasText(to)) {
            return new ArrayList<>();
        }

        String[] splitIds = to.split(",");
        return Arrays.stream(splitIds)
                .map(StringUtils::trimWhitespace)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }


    @PostMapping("/sendEmail")
    public ModelAndView sendEmail(@AuthenticationPrincipal OAuth2User principal,@RequestBody MultiValueMap<String, String> formData) {
            if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
                return new ModelAndView("redirect:/");
            }
            
            String from = principal.getAttribute("login");
            String subject = formData.getFirst("subject");
            List<String> toIds = splitIds(formData.getFirst("toIds"));
            String body = formData.getFirst("body");

            emailService.sendEmail(from,toIds,subject,body);

            return new ModelAndView(("redirect:/"));

    }
}



