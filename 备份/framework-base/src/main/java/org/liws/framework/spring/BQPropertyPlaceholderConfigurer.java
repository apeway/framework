package org.liws.framework.spring;


import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.liws.framework.util.UtilTools;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.context.support.ServletContextResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
<bean name="propertyPlaceholderConfigurer" class="org.liws.framework.spring.BQPropertyPlaceholderConfigurer">
    <property name="location" value="#{systemProperties['bq_config']}/serverConfig.properties"/>
</bean>
 */
public class BQPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private Resource[] resources;

	@Override
	public void setLocation(Resource location) {
		if (location.exists()) {
			super.setLocation(location);
			this.resources = new Resource[]{location};
		} else {
			FileSystemResource fileSystemResource = new FileSystemResource(
					new File(UtilTools.getBQConfig() + File.separator + "serverConfig.properties"));
			if(fileSystemResource.exists()){
				super.setLocation(fileSystemResource);
				this.resources = new Resource[]{fileSystemResource};

			}else {
				PathMatchingResourcePatternResolver resolover = new PathMatchingResourcePatternResolver();
				try {
					Resource[] resources = resolover.getResources("classpath*:serverConfig.properties");
					if (resources.length > 0 && resources[0] != null && resources[0].exists()) {
						super.setLocation(resources[0]);
						this.resources = new Resource[]{resources[0]};
					} else {
						resources = resolover.getResources("classpath*:serverTestConfig.properties");
						if (resources.length > 0 && resources[0] != null && resources[0].exists()) {
							super.setLocation(resources[0]);
							this.resources = new Resource[]{resources[0]};
						} else {
							throw new BusinessRuntimeException("can not find serverConfig.properties file");
						}

					}
				} catch (IOException e) {
					BQLogger.error(e);
				}
			}
		}

	}

	@Override
	protected void loadProperties(Properties props) throws IOException {
		try {
			super.loadProperties(props);
		} catch (FileNotFoundException ex) {
			if (this.resources != null) {
				for (Resource location : this.resources) {
					try {
						if (location.exists()) {
							Properties props2 = new Properties();
							try (InputStream in = Files.newInputStream(Paths.get(location.getURI()))) {
								props2.load(in);
							}
							props.putAll(props2);
						} else if (location instanceof ServletContextResource) {
							ServletContextResource scr = (ServletContextResource) location;
							File file = new File(scr.getPath());
							if (file.exists()) {
								Properties props2 = new Properties();
								try (InputStream in = new FileInputStream(file)) {
									props2.load(in);
								}
								props.putAll(props2);
							}
						}
					} catch (IOException ex2) {

						throw ex;
					}
				}
			}
		}

	}
}
