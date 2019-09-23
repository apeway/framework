package org.liws.framework.i18n;


import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class BQResourceBundleMessageSource extends ResourceBundleMessageSource {
	
	public BQResourceBundleMessageSource() {
		setBasenames(scanMessages());
	}

	private String[] scanMessages() {
		String[] result = new String[0];

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = null;
        try {
            resources = resolver.getResources("classpath*:/messages/*.properties");
        } catch (IOException e) {
            //BQLogger.error(e);
        }

        Set<String> set = new HashSet<String>();
        if ((resources != null) && (resources.length > 0)) {
            for (Resource file : resources) {
                String name = file.getFilename();
                if (name.endsWith("_zh_CN.properties")) {
                    name = name.substring(0, name.length() - 17);
                } else if (name.endsWith("_en_US.properties")) {
                    name = name.substring(0, name.length() - 17);
                } else if (name.endsWith(".properties")){
                    name = name.substring(0, name.length() - 11);
                }
                set.add("messages/" + name);
            }
        }
        result = (String[]) set.toArray(result);
        return result;

	}
}
