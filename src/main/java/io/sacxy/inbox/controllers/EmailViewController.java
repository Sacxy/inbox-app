package io.sacxy.inbox.controllers;

import io.sacxy.inbox.email.Email;
import io.sacxy.inbox.email.EmailRepository;
import io.sacxy.inbox.folders.Folder;
import io.sacxy.inbox.folders.FolderRepository;
import io.sacxy.inbox.folders.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailViewController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private EmailRepository emailRepository;

    @GetMapping(value = "/emails/{id}")
    public String emailView(@AuthenticationPrincipal OAuth2User principal, Model model, @PathVariable UUID id) {
        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        } else {


            // Fetch Folders
            String userId = principal.getAttribute("login");
            List<Folder> userFolders = folderRepository.findAllById(userId);
            model.addAttribute("userFolders", userFolders);

            List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
            model.addAttribute("defaultFolders", defaultFolders);

            Optional<Email> optionalEmail = emailRepository.findById(id);
            if(!optionalEmail.isPresent()) {
                return "inbox-page";
            }

            Email email = optionalEmail.get();
            String toIds = String.join(", ", email.getTo());

            model.addAttribute("toIds", toIds);
            model.addAttribute("email", email);




            return "email-page";
        }
    }
}
