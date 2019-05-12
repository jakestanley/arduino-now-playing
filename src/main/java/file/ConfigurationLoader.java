package file;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import configuration.Configuration;
import configuration.Secrets;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;

@Log
@AllArgsConstructor
public class ConfigurationLoader {

    private static final String DEFAULT_CONFIG_PATH = "config.yaml";

    public static Configuration load(final String secretsFilePath) {
        final Configuration configuration = loadDefaultConfiguration();
        configuration.setSecrets(loadSecretsConfiguration(secretsFilePath));

        return configuration;
    }

    public static Configuration loadDefaultConfiguration() {

        final Yaml yaml = new Yaml();

        final String content;
        try {
            content = Resources.toString(Resources
                .getResource(DEFAULT_CONFIG_PATH), Charsets.UTF_8);
            return yaml.loadAs(content, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // TODO handle null
    }

    public static Secrets loadSecretsConfiguration(final String secretsFilePath) {

        final Yaml yaml = new Yaml();

        try {
            final FileInputStream fileInputStream = new FileInputStream(secretsFilePath);
            final String content = IOUtils.toString(fileInputStream, "UTF-8");

            return yaml.loadAs(content, Secrets.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // TODO handle null
    }
}
