package io.sacxy.inbox;

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

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class InboxApp {

	@Autowired
	FolderRepository folderRepository;

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
	}

}
