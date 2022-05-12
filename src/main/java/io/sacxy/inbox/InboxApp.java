package io.sacxy.inbox;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.sacxy.inbox.email.Email;
import io.sacxy.inbox.email.EmailRepository;
import io.sacxy.inbox.emaillist.EmailListItem;
import io.sacxy.inbox.emaillist.EmailListItemKey;
import io.sacxy.inbox.emaillist.EmailListItemRepository;
import io.sacxy.inbox.folders.Folder;
import io.sacxy.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class InboxApp {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	EmailListItemRepository emailListItemRepository;

	@Autowired
	EmailRepository emailRepository;

	public static void main(String[] args) {
		SpringApplication.run(InboxApp.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {
		folderRepository.save(new Folder("Sacxy", "inbox", "blue"));
		folderRepository.save(new Folder("Sacxy", "inb", "red"));
		folderRepository.save(new Folder("Sacxy", "in", "yello"));

		for(int i = 0; i < 10 ; i++) {
			EmailListItemKey key = new EmailListItemKey();
			key.setId("Sacxy");
			key.setLabel("Inbox");
			key.setTimeUUID(Uuids.timeBased());

			EmailListItem item = new EmailListItem();
			item.setKey(key);
			item.setTo(Arrays.asList("Sacxy"));
			item.setSubject("Subject" + i);
			item.setRead(true);

			emailListItemRepository.save(item);

			Email email = new Email();

			email.setId(key.getTimeUUID());
			email.setSubject(item.getSubject());
			email.setTo(item.getTo());
			email.setBody("Body" + i);
			email.setFrom("Sacxy");

			emailRepository.save(email);

		}
	}

}
